package teamavanti.view;

import javafx.animation.PauseTransition;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

// ─── Panel temporal de inicio de sesión ──────────────────────────────────────────────
public class LoggingInPanel extends StackPane {

    private MainFrame mainFrame;

    public LoggingInPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        createUI();
    }

    private void createUI() {
        setStyle("-fx-background-color: #0f0f1a;");

        Label lbl = new Label("Iniciando sesión en la aplicación...\n\nPor favor, espere");
        lbl.setStyle("-fx-font-size: 32px; -fx-text-fill: #fff; -fx-font-weight: bold;");
        lbl.setAlignment(javafx.geometry.Pos.CENTER);

        getChildren().add(lbl);

        // Pausar 2 segundos y luego redirigir según el rol
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> {
            teamavanti.model.User currentUser = teamavanti.util.SessionManager.getCurrentUser();
            if (currentUser != null && "admin".equals(currentUser.getRol())) {
                mainFrame.showAdminPanel();
            } else {
                mainFrame.showUserPanel();
            }
        });
        delay.play();
    }
}
