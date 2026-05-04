package teamavanti.bbdd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import teamavanti.model.Movie;

public class DatabaseManager {

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/avanti";
    private static final String USUARIO = "root";
    private static final String PASSWORD = "";

    // Instancia única (Singleton)
    private static DatabaseManager instance;

    // Constructor privado
    private DatabaseManager() {
    }

    /** Devuelve la instancia única de DatabaseManager. */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // ─── Conexión ───────────────────────────────────────────────────────────────

    /**
     * Abre y devuelve una nueva conexión a la base de datos.
     * Debe cerrarse siempre con {@link #closeConnection(Connection)}.
     */
    public Connection connectToDb() {
        try {
            Class.forName(DRIVER);
            Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
            System.out.println("Conexión OK");
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("Error: driver JDBC no encontrado.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos.");
            e.printStackTrace();
        }
        return null;
    }

    /** Cierra la conexión pasada como parámetro de forma segura. */
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión.");
                e.printStackTrace();
            }
        }
    }

    // ─── Métodos de prueba (provisional) ────────────────────────────────────────

    /**
     * Recupera todas las películas e imprime cada una por consola.
     * Método de prueba; la lógica real está en MovieFunctions.
     */
    public void getData() throws SQLException {
        Connection connect = connectToDb();
        if (connect == null)
            return;

        try {
            String sql = "SELECT * FROM pelicula";
            Statement stmt = connect.createStatement();
            if (stmt.execute(sql)) {
                ResultSet rs = stmt.getResultSet();
                while (rs.next()) {
                    Movie movie = new Movie(
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
                            "");
                    System.out.println(movie);
                }
                System.out.println("Datos recuperados correctamente.");
            }
            stmt.close();
        } finally {
            closeConnection(connect);
        }
    }

    /**
     * Inserta una película en la base de datos.
     * Usa la tabla 'pelicula' con columnas: id, titulo, director, ano, stock,
     * precio.
     */
    public void insertPelicula(Movie movie) throws SQLException {
        Connection connect = connectToDb();
        if (connect == null)
            return;

        String sql = "INSERT INTO pelicula (titulo, director, ano, sinopsis, duracion, precio, imagen, video, disponible, id_genero) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connect.prepareStatement(sql)) {
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
            System.out.println("Película insertada correctamente.");
        } finally {
            closeConnection(connect);
        }
    }
}
