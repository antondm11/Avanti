package teamavanti.bbdd;

//Importar clases de la base de datos
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import teamavanti.bbdd.DatabaseManager;

//Importar clase Movie de las películas
import teamavanti.model.Movie;

//Clase para los métodos que permitan añadir o eliminar películas, por ejemplo
public class MovieFunctions {
    // Método para insertar películas
    public void insertMovie(Movie movie) throws SQLException {

        // Instanciar la clase DatabseManager para utilizar sus métodos de conexión
        // Revisar por si viene mejor usar el Patrón Singleton
        DatabaseManager dbManager = new DatabaseManager();
        Connection connect = dbManager.connectToDb();

        // ***Recordar que falta revisar la adición de registros cuando esté lista la
        // BD***
        try {
            String sql = "INSERT INTO pelicula (id, titulo, director, ano, genero, stock, alquilada) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = connect.prepareStatement(sql);
            // Falta actualizar cada método de acuerdo a la base de datos
            ps.setInt(1, movie.id);
            ps.setString(2, movie.titulo);
            ps.setString(3, movie.director);
            ps.setInt(4, movie.ano);
            ps.setString(5, movie.genero);
            ps.setInt(6, movie.stock);
            ps.setBoolean(7, movie.alquilada);
            ps.executeUpdate();
            ps.close();
            System.out.println("Película añadida correctamente");
        } finally {
            // Cerrar la conexión tras cada método a través de la instancia de
            // DatabaseManager
            dbManager.closeConnection(connect);
        }
    }

    // Método para eliminar películas
    public void deleteMovie(Movie movie) throws SQLException {

        DatabaseManager dbManager = new DatabaseManager();
        Connection connect = dbManager.connectToDb();

        try {
            String sql = "DELETE FROM pelicula WHERE id = ?";
            PreparedStatement ps = connect.prepareStatement(sql);
            ps.setInt(1, movie.id);
            ps.executeUpdate();
            ps.close();
            System.out.println("Película eliminada correctamente");
        } finally {
            dbManager.closeConnection(connect);
        }

    }

    // Método para visualizar las películas
    public void getMovies() throws SQLException {
        DatabaseManager dbManager = new DatabaseManager();
        Connection connect = dbManager.connectToDb();

        try {
            String sql = "SELECT * FROM pelicula";
            PreparedStatement ps = connect.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Movie movie = new Movie(rs.getInt("id"), rs.getString("titulo"), rs.getString("director"),
                        rs.getInt("ano"), rs.getString("genero"), rs.getInt("stock"), rs.getBoolean("alquilada"));
                System.out.println(movie.toString());
            }
            ps.close();
            System.out.println("Películas recuperadas correctamente");
        } finally {
            dbManager.closeConnection(connect);
        }
    }

    // Método para contar películas por género, director, etc.
    public void countMovies() throws SQLException {
        DatabaseManager dbManager = new DatabaseManager();
        Connection connect = dbManager.connectToDb();

        try {
            // Ejemplo para contar por género (*revisarlo*)
            String sql = "SELECT COUNT(*) FROM pelicula GROUP BY genero";
            PreparedStatement ps = connect.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getInt("COUNT(*)"));
            }
            ps.close();
            System.out.println("Películas contadas correctamente");
        } finally {
            dbManager.closeConnection(connect);
        }
    }

}
