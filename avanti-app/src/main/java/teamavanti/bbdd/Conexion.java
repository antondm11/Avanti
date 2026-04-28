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

public class Conexion {

    // driver JDBC
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    // dirección de la BD MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/videoclub";
    // usuario y contraseña de acceso a la BD
    private static final String USUARIO = "root";
    private static final String PASSWORD = "";

    public Connection conectar() {
        Connection conexion = null;

        try {
            Class.forName(DRIVER);
            conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
            System.out.println("Conexión OK");

        } catch (ClassNotFoundException e) {
            System.out.println("Error al cargar el controlador");
            e.printStackTrace();

        } catch (SQLException e) {
            System.out.println("Error en la conexión");
            e.printStackTrace();
        }

        return conexion;
    }

    // Método para cerrar la conexión (siempre que se deje de usar la BD)
    public void cerrarConexion(Connection conection) {
        try {
            // Cierre de la conexión
            conection.close();
        } catch (SQLException e) {
            System.err.println("Se ha producido un error al cerrar la conexión");

        }
    }

    // Método para insertar películas *Falta insertar datos*
    public void insertPelicula(Pelicula p) throws SQLException {
        Connection conexion = conectar();
        PreparedStatement ps = conexion.prepareStatement(sql);
        ps.setInt(1, p.id);
        ps.setString(2, p.titulo);
        ps.setString(3, p.director);
        ps.setInt(4, p.ano);
        ps.setString(5, p.genero);
        ps.setInt(6, p.stock);
        ps.setBoolean(7, p.alquilada);
        ps.executeUpdate();
        ps.close();
        try {
            
            String consultasInserccion = "INSERT INTO pelicula VALUES(*registros , *);";
            
            
            
            
            System.out.println(consultasInserccion);
            // Crear el Statement para realizar la consulta
            Statement consul = conexion.createStatement();
            // Ejecutar la consulta
            consul.executeUpdate(consultasInserccion);
            System.out.println("Datos insertados correctamente");
            // Cerrar el Statement
            consul.close();
        } finally {
            // Cerrar la conexión
            cerrarConexion(conexion);
        }
    }

    public void getData() throws SQLException {
        Connection conexion = conectar();

        if (conexion != null) {
            try {
                // Datos a consultar -- Prueba de consultar toda la tabla peliculas
                String consultasSeleccion = "SELECT * FROM peliculas";
                System.out.println(consultasSeleccion);
                Statement consul = conexion.createStatement();
                // Ejecución de la consulta
                if (consul.execute(consultasSeleccion)) {
                    ResultSet resultset = consul.getResultSet();
                    while (resultset.next()) {
                        Pelicula pelicula = new Pelicula(resultset.getInt("id"), resultset.getString("titulo"),
                                resultset.getString("director"), resultset.getInt("ano"), resultset.getString("genero"),
                                resultset.getInt("stock"), resultset.getBoolean("alquilada"));
                        System.out.println(pelicula.toString());

                    }

                    System.out.println("Datos recuperados correctamente");
                }
                // Cierre del Statement
                consul.close();

            } finally {
                // Cierre de la conexión
                cerrarConexion(conexion);
            }
        }
    }

    // Llevar este main a una clase Main
    public static void main(String[] args) {

        Conexion conexion = new Conexion();
        Connection cn = null;

        try {
            cn = conexion.conectar();
            conexion.insertData();
            conexion.getData();

        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

}
