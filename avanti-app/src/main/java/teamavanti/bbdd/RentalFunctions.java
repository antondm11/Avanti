package teamavanti.bbdd;

//Importar clases de la base de datos
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//Importar clase Rental de los alquileres
import teamavanti.model.Rental;
//Importar clases Movie y User
import teamavanti.model.Movie;
import teamavanti.model.User;

//Clase para los métodos que permitan añadir o eliminar alquileres, por ejemplo
public class RentalFunctions {

    // Método para insertar alquileres
    public void insertRental(Rental rental) throws SQLException {
        // Instanciar la clase DatabaseManager para utilizar sus métodos de conexión
        DatabaseManager dbManager = new DatabaseManager();
        Connection connect = dbManager.connectToDb();

        try {
            String sql = "INSERT INTO alquileres (id, id_usuario, id_pelicula, fecha_alquiler, fecha_devolucion, precio) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = connect.prepareStatement(sql);
            ps.setInt(1, rental.id);
            ps.setInt(2, rental.id_usuario);
            ps.setInt(3, rental.id_pelicula);
            ps.setDate(4, rental.fecha_alquiler);
            ps.setDate(5, rental.fecha_devolucion);
            ps.setDouble(6, rental.precio);
            ps.executeUpdate();
            ps.close();
            System.out.println("Alquiler insertado correctamente");
        } finally {
            dbManager.closeConnection(connect);
        }

    }

    // Método para eliminar alquileres
    public void deleteRental(Rental rental) {

    }

    // Método para ver los alquileres actuales
    public void getRentals() {

    }

    // Método para alquilar película
    public void rentMovie(User user, Movie movie) {
        // Debe buscar la película y restarle 1 al stock
        // No se puede alquilar si el stock es 0
        // Debe comprobar que el usuario existe
        // Debe registrarlo como alquilada y poner la fecha
    }

    // Método para devolver película
    public void returnMovie(int id) {
        // Debe devolver la película y sumarla al stock
        // No se borrará el registro, se marcará como que se ha devuelto y ya se podrá
        // alquilar otra vez

    }

}
