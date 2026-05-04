package teamavanti.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import teamavanti.bbdd.DatabaseManager;
import java.sql.Connection;

/**
 * Punto de entrada JavaFX de la aplicación Avanti.
 * Sustituye al antiguo MainFrame (Swing).
 */
public class MainFrame extends Application {

    private Stage primaryStage;
    private Scene sceneHome;
    private Scene sceneSignIn;
    private Scene sceneSignUp;
    private Scene sceneUser;
    private Scene sceneAdmin;

    // Paneles
    private HomeScreen homeScreen;
    private SignInPanel signInPanel;
    private SignUpPanel signUpPanel;
    private UserPanel userPanel;
    private AdminView adminView;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        stage.setTitle("Avanti · Videoclub Digital");
        stage.setWidth(1100);
        stage.setHeight(700);
        stage.setResizable(false);

        // ── Verificar conexión a MySQL al arrancar ───────────────────────────────────
        Connection testConn = DatabaseManager.getInstance().connectToDb();
        if (testConn == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error de conexión a la base de datos");
            alert.setHeaderText("⚠ No se pudo conectar a MySQL");
            alert.setContentText(
                    "La aplicación no puede conectar con la base de datos.\n\n" +
                    "Comprueba que:\n" +
                    "  • MySQL / XAMPP está en marcha\n" +
                    "  • Existe la base de datos 'avanti'\n" +
                    "    (ejecuta database/avanti.sql en MySQL)\n" +
                    "  • El usuario 'root' sin contraseña tiene acceso\n\n" +
                    "URL: jdbc:mysql://localhost:3306/avanti\n" +
                    "Usuario: root / Contraseña: (vacía)");
            alert.showAndWait();
        } else {
            DatabaseManager.getInstance().closeConnection(testConn);
        }
        // ────────────────────────────────────────────────────────────
        // Crear las pantallas pasándose a sí mismo (para poder navegar)
        homeScreen = new HomeScreen(this);
        signInPanel = new SignInPanel(this);
        signUpPanel = new SignUpPanel(this);
        userPanel = new UserPanel(this);
        adminView = new AdminView(this);

        // Escenas
        sceneHome = new Scene(homeScreen);
        sceneSignIn = new Scene(signInPanel);
        sceneSignUp = new Scene(signUpPanel);
        sceneUser = new Scene(userPanel);
        sceneAdmin = new Scene(adminView);

        // Iniciar en la pantalla de inicio
        showHome();
        stage.show();
    }

    // ─── Navegación ─────────────────────────────────────────────────────────────

    public void showHome() {
        primaryStage.setScene(sceneHome);
    }

    public void showSignIn() {
        primaryStage.setScene(sceneSignIn);
    }

    public void showSignUp() {
        primaryStage.setScene(sceneSignUp);
    }

    public void showUserPanel() {
        // Reconstruir UserPanel para reflejar sesión actualizada
        userPanel = new UserPanel(this);
        sceneUser = new Scene(userPanel);
        primaryStage.setScene(sceneUser);
    }

    public void showAdminPanel() {
        adminView = new AdminView(this);
        sceneAdmin = new Scene(adminView);
        primaryStage.setScene(sceneAdmin);
    }

    public void logout() {
        teamavanti.util.SessionManager.logout();
        showHome();
    }

    // ─── Mantener retrocompatibilidad con showPanel() que usaba Swing ───────────
    /** @deprecated Usar los métodos show*() específicos. */
    @Deprecated
    public void showPanel(String panel) {
        switch (panel) {
            case "home":
                showHome();
                break;
            case "signIn":
                showSignIn();
                break;
            case "signUp":
                showSignUp();
                break;
            case "user":
                showUserPanel();
                break;
            case "admin":
                showAdminPanel();
                break;
        }
    }
}
