package teamavanti.bbdd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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

}
