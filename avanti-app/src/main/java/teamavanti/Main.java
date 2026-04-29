package teamavanti;

//Importaciones para MainFrame y la inicialización de la GUI
import teamavanti.view.MainFrame;
import javax.swing.SwingUtilities;

//Importar la clase DatabaseManager para conectarse a la BD
import teamavanti.bbdd.DatabaseManager;

public class Main {

    public static void main(String[] args) {

        // Conectarse a la base de datos mediante la instancia de su clase y el método
        // para conectar
        // DatabaseManager.getInstance().connectToDb();
        // DESCOMENTAR LAS 3 LÍNEAS Y PROBARLO CUANDO ESTÉ LA BASE DE DATOS

        // Inicializar la interfaz gráfica en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);

            // Mostrar por defecto el HomePanel al iniciar la App
            frame.showPanel("home");
        });

    }

}
