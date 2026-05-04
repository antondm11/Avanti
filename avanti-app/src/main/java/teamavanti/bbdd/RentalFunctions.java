package teamavanti.bbdd;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import teamavanti.model.Rental;

/**
 * Operaciones sobre la tabla alquiler y la vista v_alquileres.
 */
public class RentalFunctions {

    // ─── Alquilar película ──────────────────────────────────────────────────────

    /**
     * Registra un nuevo alquiler activo.
     *
     * La fecha de alquiler se asigna por defecto en la BBDD.
     * La fecha de devolución se calcula a 7 días.
     * El precio_pagado lo asigna el trigger desde pelicula.precio.
     * La disponibilidad de la película también la actualiza el trigger.
     *
     * @param idPelicula ID de la película que se alquila
     * @param idUsuario  ID del usuario que alquila
     */
    public void rentMovie(int idPelicula, int idUsuario) throws SQLException {
        Connection conn = DatabaseManager.getInstance().connectToDb();
        if (conn == null)
            throw new SQLException("No se pudo conectar a la base de datos (rentMovie).");

        String sql = """
                INSERT INTO alquiler (fecha_devolucion, id_pelicula, id_usuario)
                VALUES (DATE_ADD(CURDATE(), INTERVAL 7 DAY), ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPelicula);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
            System.out.println("Alquiler registrado correctamente.");
        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }
    }

    // ─── Devolver película ──────────────────────────────────────────────────────

    /**
     * Marca un alquiler como devuelto.
     *
     * El trigger de la BBDD se encarga de volver a poner la película como
     * disponible.
     *
     * @param idAlquiler ID del alquiler que se devuelve
     */
    public void returnMovie(int idAlquiler) throws SQLException {
        Connection conn = DatabaseManager.getInstance().connectToDb();
        if (conn == null)
            throw new SQLException("No se pudo conectar a la base de datos (returnMovie).");

        String sql = """
                UPDATE alquiler
                SET estado = 'DEVUELTO'
                WHERE id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAlquiler);
            ps.executeUpdate();
            System.out.println("Película devuelta correctamente.");
        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }
    }

    // ─── Actualizar vencidos ────────────────────────────────────────────────────

    /**
     * Marca como vencidos los alquileres activos cuya fecha de devolución ya ha
     * pasado.
     * También recalcula la multa.
     *
     * Este método sustituye al EVENT de MySQL, para evitar problemas de permisos.
     */
    public void updateExpiredRentals() throws SQLException {
        Connection conn = DatabaseManager.getInstance().connectToDb();
        if (conn == null)
            throw new SQLException("No se pudo conectar a la base de datos (updateExpiredRentals).");

        String sql = """
                UPDATE alquiler
                SET estado = 'VENCIDO',
                    multa = GREATEST(0, DATEDIFF(CURDATE(), fecha_devolucion)) * 0.50
                WHERE estado = 'ACTIVO'
                  AND fecha_devolucion < CURDATE()
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
            System.out.println("Alquileres vencidos actualizados.");
        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }
    }

    // ─── Obtener todos los alquileres de un usuario ─────────────────────────────

    /**
     * Devuelve todos los alquileres de un usuario usando la vista v_alquileres.
     */
    public List<Rental> getRentalsByUser(int idUsuario) throws SQLException {
        List<Rental> list = new ArrayList<>();

        updateExpiredRentals();

        Connection conn = DatabaseManager.getInstance().connectToDb();
        if (conn == null)
            throw new SQLException("No se pudo conectar a la base de datos (getRentalsByUser).");

        String sql = """
                SELECT *
                FROM v_alquileres
                WHERE id_usuario = ?
                ORDER BY fecha_alquiler DESC
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRentalFromView(rs));
                }
            }
        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }

        return list;
    }

    // ─── Obtener alquileres activos ─────────────────────────────────────────────

    public List<Rental> getActiveRentalsByUser(int idUsuario) throws SQLException {
        List<Rental> list = new ArrayList<>();

        // updateExpiredRentals() se llama una sola vez desde el nivel de UI
        Connection conn = DatabaseManager.getInstance().connectToDb();
        if (conn == null)
            throw new SQLException("No se pudo conectar a la base de datos (getActiveRentalsByUser).");

        String sql = """
                SELECT *
                FROM v_alquileres
                WHERE id_usuario = ?
                  AND estado = 'ACTIVO'
                ORDER BY fecha_devolucion ASC, id ASC
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRentalFromView(rs));
                }
            }
        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }

        return list;
    }

    // ─── Obtener alquileres vencidos ────────────────────────────────────────────

    public List<Rental> getExpiredRentalsByUser(int idUsuario) throws SQLException {
        List<Rental> list = new ArrayList<>();

        // updateExpiredRentals() se llama una sola vez desde el nivel de UI
        Connection conn = DatabaseManager.getInstance().connectToDb();
        if (conn == null)
            throw new SQLException("No se pudo conectar a la base de datos (getExpiredRentalsByUser).");

        String sql = """
                SELECT *
                FROM v_alquileres
                WHERE id_usuario = ?
                  AND estado = 'VENCIDO'
                ORDER BY fecha_devolucion ASC, id ASC
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRentalFromView(rs));
                }
            }
        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }

        return list;
    }

    // ─── Obtener historial de devueltas ─────────────────────────────────────────

    public List<Rental> getReturnedRentalsByUser(int idUsuario) throws SQLException {
        List<Rental> list = new ArrayList<>();

        Connection conn = DatabaseManager.getInstance().connectToDb();
        if (conn == null)
            throw new SQLException("No se pudo conectar a la base de datos (getReturnedRentalsByUser).");

        String sql = """
                SELECT *
                FROM v_alquileres
                WHERE id_usuario = ?
                  AND estado = 'DEVUELTO'
                ORDER BY fecha_alquiler DESC, id DESC
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRentalFromView(rs));
                }
            }
        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }

        return list;
    }

    // ─── Obtener todos los alquileres para admin ────────────────────────────────

    public List<Rental> getAllRentals() throws SQLException {
        List<Rental> list = new ArrayList<>();

        updateExpiredRentals();

        Connection conn = DatabaseManager.getInstance().connectToDb();
        if (conn == null)
            throw new SQLException("No se pudo conectar a la base de datos (getAllRentals).");

        String sql = """
                SELECT *
                FROM v_alquileres
                ORDER BY fecha_alquiler DESC
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRentalFromView(rs));
            }

        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }

        return list;
    }

    // ─── Cálculos ───────────────────────────────────────────────────────────────

    /**
     * Calcula el total gastado por un usuario.
     * Incluye precio_pagado + multa registrada.
     */
    public double calculateTotalSpentByUser(int idUsuario) throws SQLException {
        Connection conn = DatabaseManager.getInstance().connectToDb();
        if (conn == null)
            return 0.0;

        String sql = """
                SELECT COALESCE(SUM(precio_pagado + multa), 0) AS total
                FROM alquiler
                WHERE id_usuario = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }

        return 0.0;
    }

    /**
     * Calcula los ingresos totales del videoclub.
     * Incluye precio_pagado + multa.
     */
    public double calculateTotalRevenue() throws SQLException {
        Connection conn = DatabaseManager.getInstance().connectToDb();
        if (conn == null)
            return 0.0;

        String sql = """
                SELECT COALESCE(SUM(precio_pagado + multa), 0) AS total
                FROM alquiler
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("total");
            }

        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }

        return 0.0;
    }

    /**
     * Calcula solo las multas acumuladas.
     */
    public double calculateTotalFines() throws SQLException {
        Connection conn = DatabaseManager.getInstance().connectToDb();
        if (conn == null)
            return 0.0;

        String sql = """
                SELECT COALESCE(SUM(multa), 0) AS total_multas
                FROM alquiler
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("total_multas");
            }

        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }

        return 0.0;
    }

    // ─── Método de borrado opcional ─────────────────────────────────────────────

    /**
     * Borra un alquiler por ID.
     * Normalmente no se usará desde cliente, pero puede servir para admin/pruebas.
     */
    public void deleteRental(int idAlquiler) throws SQLException {
        Connection conn = DatabaseManager.getInstance().connectToDb();
        if (conn == null)
            return;

        String sql = "DELETE FROM alquiler WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAlquiler);
            ps.executeUpdate();
            System.out.println("Alquiler eliminado correctamente.");
        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }
    }

    // ─── Mapper ─────────────────────────────────────────────────────────────────

    private Rental mapRentalFromView(ResultSet rs) throws SQLException {
        Rental rental = new Rental();

        rental.setId(rs.getInt("id"));
        rental.setIdUsuario(rs.getInt("id_usuario"));
        rental.setIdPelicula(rs.getInt("id_pelicula"));

        Date fechaAlquiler = rs.getDate("fecha_alquiler");
        Date fechaDevolucion = rs.getDate("fecha_devolucion");

        rental.setFechaAlquiler(fechaAlquiler != null ? fechaAlquiler.toLocalDate() : null);
        rental.setFechaDevolucion(fechaDevolucion != null ? fechaDevolucion.toLocalDate() : null);

        rental.setEstado(rs.getString("estado"));

        // En el modelo Rental el campo se llama precio (compatibilidad)
        rental.setPrecio(rs.getDouble("precio_pagado"));

        // Usamos multa_actual si existe en la vista, si no, multa
        double multa;
        try {
            multa = rs.getDouble("multa_actual");
        } catch (SQLException e) {
            multa = rs.getDouble("multa");
        }
        rental.setMulta(multa);

        // Asignar el título de la película desde la vista (columna 'pelicula')
        try {
            String titulo = rs.getString("pelicula");
            if (titulo != null) {
                teamavanti.model.Movie m = new teamavanti.model.Movie();
                m.setId(rental.getIdPelicula());
                m.setTitulo(titulo);
                rental.setPelicula(m);
            }
        } catch (SQLException ignored) {
            // Si la columna no existe, se deja null
        }

        return rental;
    }

    /**
     * Comprueba si un usuario tiene un alquiler ACTIVO o VENCIDO para una película
     * concreta.
     */
    public boolean hasActiveRental(int idUsuario, int idPelicula) throws SQLException {
        Connection conn = DatabaseManager.getInstance().connectToDb();
        if (conn == null)
            return false;

        String sql = """
                SELECT COUNT(*) AS cnt
                FROM alquiler
                WHERE id_usuario = ?
                  AND id_pelicula = ?
                  AND estado IN ('ACTIVO', 'VENCIDO')
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idPelicula);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("cnt") > 0;
            }
        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }
    }
}
