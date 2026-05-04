package teamavanti;

import javafx.application.Application;
import teamavanti.bbdd.DatabaseManager;
import teamavanti.view.MainFrame;

/*
 * Punto de entrada principal de la aplicación Avanti.
 * Lanza la aplicación JavaFX a través de MainFrame.
 */

/*
 * === Comentario para Soraya: ===
 * 
 * -- Puedes iniciar sesión con el usuario cliente de prueba:
 * Usuario: "cliente@avanti.com"
 * Contraseña: "cliente123"
 * 
 * -- Para ver los paneles de admin, entra con este usuario:
 * Usuario: "admin@avanti.com"
 * Contraseña: "admin123"
 *
 * Para evitar posibles malentendidos con espacios o minusculas,
 * pon tal cual lo que está entre comillas
 */

public class Main {

    public static void main(String[] args) {
        DatabaseManager.getInstance().connectToDb();

        // Lanzar la aplicación JavaFX
        Application.launch(MainFrame.class, args);
    }
}
