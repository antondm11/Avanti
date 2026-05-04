package teamavanti.view;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

// ─── Panel temporal de despedida ──────────────────────────────────────────────
public class ExitPanel extends StackPane {

    public ExitPanel() {
        createUI();
    }

    private void createUI() {
        setStyle("-fx-background-color: #0f0f1a;");

        Label lbl = new Label("¡Gracias por utilizar la App de Avanti!\n\n¡Hasta pronto!");
        lbl.setStyle("-fx-font-size: 32px; -fx-text-fill: #fff; -fx-font-weight: bold;");
        lbl.setAlignment(javafx.geometry.Pos.CENTER);

        getChildren().add(lbl);

        // Pausar 3 segundos y luego cerrar la aplicación
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> Platform.exit());
        delay.play();
    }
}
