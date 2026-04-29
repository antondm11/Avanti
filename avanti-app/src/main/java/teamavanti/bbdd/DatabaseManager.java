package teamavanti.bbdd;

// Importaciones
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// Clases de la base de datos importadas
import teamavanti.model.Movie;
import teamavanti.model.Rental;
import teamavanti.model.User;

import java.sql.PreparedStatement;

public class DatabaseManager {

    // driver JDBC
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    // dirección de la BD MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/videoclub";
    // usuario y contraseña de acceso a la BD
    private static final String USUARIO = "root";
    private static final String PASSWORD = "";

    // Instancia única de la clase DatabaseManager (Singleton)
    private static DatabaseManager instance;

    // Constructor privado para que su objeto sólo pueda ser llamado a través del método getInstance()
    private DatabaseManager() {
    }

    // Método para obtener la instancia única de la clase DatabaseManager
    // Al llamarlo para conectar (p.ej) se hará con DatabaseManager.getInstance() y el método de conectar, 
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // Método para conectarse a la base de datos
    public Connection connectToDb() {
        Connection connect = null;

        try {
            Class.forName(DRIVER);
            connect = DriverManager.getConnection(URL, USUARIO, PASSWORD);
            System.out.println("Conexión OK");

        } catch (ClassNotFoundException e) {
            System.out.println("Error al cargar el controlador");
            e.printStackTrace();

        } catch (SQLException e) {
            System.out.println("Error en la conexión");
            e.printStackTrace();
        }

        return connect;
    }

    // Método para cerrar la conexión (siempre que se deje de usar la BD)
    public void closeConnection(Connection connection) {
        try {
            // Cierre de la conexión
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Se ha producido un error al cerrar la conexión");

        }
    }


    // ESTOS MÉTODOS HABRÍA QUE TRASLADARLOS A LA CLASE MovieFunctions y demás
    // (Rental y User, los que correspondan)

    // Método para insertar películas *Falta insertar datos*
    public void insertPelicula(Movie movie) throws SQLException {
        Connection connect = connectToDb();
        PreparedStatement ps = connect.prepareStatement(sql);
        ps.setInt(1, movie.id);
        ps.setString(2, movie.titulo);
        ps.setString(3, movie.director);
        ps.setInt(4, movie.ano);
        ps.setString(5, movie.genero);
        ps.setInt(6, movie.stock);
        ps.setBoolean(7, movie.alquilada);
        ps.executeUpdate();
        ps.close();
        try {

            String consultasInserccion = "INSERT INTO pelicula VALUES(*registros , *);";

            System.out.println(consultasInserccion);
            // Crear el Statement para realizar la consulta
            Statement consul = connect.createStatement();
            // Ejecutar la consulta
            consul.executeUpdate(consultasInserccion);
            System.out.println("Datos insertados correctamente");
            // Cerrar el Statement
            consul.close();
        } finally {
            // Cerrar la conexión
            closeConnection(connect);
        }
    }

    public void getData() throws SQLException {
        Connection connect = connectToDb();

        if (connect != null) {
            try {
                // Datos a consultar -- Prueba de consultar toda la tabla peliculas
                String consultasSeleccion = "SELECT * FROM peliculas";
                System.out.println(consultasSeleccion);
                Statement consul = connect.createStatement();
                // Ejecución de la consulta
                if (consul.execute(consultasSeleccion)) {
                    ResultSet resultset = consul.getResultSet();
                    while (resultset.next()) {
                        Movie movie = new Movie(resultset.getInt("id"), resultset.getString("titulo"),
                                resultset.getString("director"), resultset.getInt("ano"), resultset.getString("genero"),
                                resultset.getInt("stock"), resultset.getBoolean("alquilada"));
                        System.out.println(movie.toString());

                    }

                    System.out.println("Datos recuperados correctamente");
                }
                // Cierre del Statement
                consul.close();

            } finally {
                // Cierre de la conexión
                closeConnection(connect);
            }
        }
    }

    // Llevar este main a una clase Main *PROVISIONAL*
    public static void main(String[] args) {

        DatabaseManager connect = new DatabaseManager();
        Connection cn = null;

        try {
            cn = connect.connectToDb();
            connect.insertPelicula();
            connect.getData();

        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

}
