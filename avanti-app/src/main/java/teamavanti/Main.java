package teamavanti;

import javafx.application.Application;
import teamavanti.bbdd.DatabaseManager;
import teamavanti.view.MainFrame;

/**
 * Punto de entrada principal de la aplicación Avanti.
 * Lanza la aplicación JavaFX a través de MainFrame.
 */
public class Main {

    public static void main(String[] args) {
        DatabaseManager.getInstance().connectToDb();

        // Lanzar la aplicación JavaFX
        Application.launch(MainFrame.class, args);
    }
}
