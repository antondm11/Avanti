package teamavanti.bbdd;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import teamavanti.model.Movie;

public class MovieFunctions {

    public List<Movie> getMovies() throws SQLException {
        List<Movie> list = new ArrayList<>();
        Connection conn = DatabaseManager.getInstance().connectToDb();

        if (conn == null)
            throw new SQLException("No se pudo conectar a la base de datos (getMovies).");

        String sql = """
                SELECT p.*, g.nombre AS genero
                FROM pelicula p
                JOIN genero g ON p.id_genero = g.id
                ORDER BY p.titulo
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapMovie(rs));
            }

        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }

        return list;
    }

    public List<Movie> getAvailableMovies() throws SQLException {
        List<Movie> list = new ArrayList<>();
        Connection conn = DatabaseManager.getInstance().connectToDb();

        if (conn == null)
            throw new SQLException("No se pudo conectar a la base de datos (getAvailableMovies).");

        String sql = """
                SELECT p.*, g.nombre AS genero
                FROM pelicula p
                JOIN genero g ON p.id_genero = g.id
                WHERE p.disponible = TRUE
                ORDER BY p.titulo
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapMovie(rs));
            }

        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }

        return list;
    }

    public List<Movie> searchMovies(String query) throws SQLException {
        List<Movie> list = new ArrayList<>();
        Connection conn = DatabaseManager.getInstance().connectToDb();

        if (conn == null)
            throw new SQLException("No se pudo conectar a la base de datos (searchMovies).");

        String sql = """
                SELECT p.*, g.nombre AS genero
                FROM pelicula p
                JOIN genero g ON p.id_genero = g.id
                WHERE p.titulo LIKE ?
                   OR p.director LIKE ?
                   OR CAST(p.ano AS CHAR) LIKE ?
                ORDER BY p.titulo
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + query + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapMovie(rs));
                }
            }

        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }

        return list;
    }

    public void insertMovie(Movie movie) throws SQLException {
        Connection conn = DatabaseManager.getInstance().connectToDb();
        if (conn == null)
            return;

        String sql = """
                INSERT INTO pelicula
                (titulo, director, ano, sinopsis, duracion, precio, imagen, video, disponible, id_genero)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, movie.getTitulo());
            ps.setString(2, movie.getDirector());
            ps.setInt(3, movie.getAno());
            ps.setString(4, movie.getSinopsis());
            ps.setInt(5, movie.getDuracion());
            ps.setDouble(6, movie.getPrecio());
            ps.setString(7, movie.getImagen());
            ps.setString(8, movie.getVideo());
            ps.setBoolean(9, movie.isDisponible());
            ps.setInt(10, movie.getIdGenero());
            ps.executeUpdate();

        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }
    }

    public void deleteMovie(int id) throws SQLException {
        Connection conn = DatabaseManager.getInstance().connectToDb();
        if (conn == null)
            return;

        String sql = "DELETE FROM pelicula WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();

        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }
    }

    public Movie getMovieById(int id) throws SQLException {
        Connection conn = DatabaseManager.getInstance().connectToDb();
        if (conn == null)
            throw new SQLException("No se pudo conectar a la base de datos (getMovieById).");

        String sql = """
                SELECT p.*, g.nombre AS genero
                FROM pelicula p
                JOIN genero g ON p.id_genero = g.id
                WHERE p.id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapMovie(rs);
                return null;
            }
        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }
    }

    private Movie mapMovie(ResultSet rs) throws SQLException {
        return new Movie(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getString("director"),
                rs.getInt("ano"),
                rs.getString("sinopsis"),
                rs.getInt("duracion"),
                rs.getDouble("precio"),
                rs.getString("imagen"),
                rs.getString("video"),
                rs.getBoolean("disponible"),
                rs.getInt("id_genero"),
                rs.getString("genero"));
    }
}
