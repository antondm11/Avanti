package teamavanti.view;

import javafx.animation.PauseTransition;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

// Panel temporal de cierre de sesión
public class LoggingOutPanel extends StackPane {

    private MainFrame mainFrame;

    public LoggingOutPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        createUI();
    }

    private void createUI() {
        setStyle("-fx-background-color: #0f0f1a;");

        Label lbl = new Label("Cerrando sesión...");
        lbl.setStyle("-fx-font-size: 32px; -fx-text-fill: #fff; -fx-font-weight: bold;");
        lbl.setAlignment(javafx.geometry.Pos.CENTER);

        getChildren().add(lbl);

        // Pausa de 1 segundo y cerrar sesión
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(e -> {
            // Cerrar sesión
            teamavanti.util.SessionManager.logout();
            // Volver a la pantalla principal
            mainFrame.showHome();
        });
        delay.play();
    }
}
