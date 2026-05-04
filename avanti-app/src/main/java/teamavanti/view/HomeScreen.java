package teamavanti.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Pantalla de bienvenida (sustituye al HomePanel de Swing).
 * Da acceso a Iniciar Sesión y Registrarse.
 */
public class HomeScreen extends BorderPane {

        private MainFrame mainFrame;

        public HomeScreen(MainFrame mainFrame) {
                this.mainFrame = mainFrame;
                createUI();
        }

        private void createUI() {
                setStyle("-fx-background-color: #0f0f1a;");

                // ── Cabecera ────────────────────────────────────────────────────────────
                HBox header = new HBox();
                header.setPadding(new Insets(20, 30, 20, 30));
                header.setAlignment(Pos.CENTER_LEFT);
                header.setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #e94560; -fx-border-width: 0 0 2 0;");

                Label lblLogo = new Label("AVANTI");
                lblLogo.setFont(Font.font("System", FontWeight.BOLD, 28));
                lblLogo.setTextFill(Color.web("#e94560"));

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label lblSlogan = new Label("Tu videoclub digital");
                lblSlogan.setTextFill(Color.web("#a0a0a0"));
                lblSlogan.setFont(Font.font(14));

                header.getChildren().addAll(lblLogo, spacer, lblSlogan);

                // ── Centro ──────────────────────────────────────────────────────────────
                VBox center = new VBox(30);
                center.setAlignment(Pos.CENTER);
                center.setPadding(new Insets(60));
                center.setStyle("-fx-background-color: #0f0f1a;");

                Label lblWelcome = new Label("Bienvenido a Avanti");
                lblWelcome.setFont(Font.font("System", FontWeight.BOLD, 42));
                lblWelcome.setTextFill(Color.WHITE);

                Label lblDesc = new Label(
                                "El mejor catálogo de películas a tu alcance.\nAlquila, disfruta y devuelve cuando quieras.");
                lblDesc.setTextFill(Color.web("#a0a0a0"));
                lblDesc.setFont(Font.font(16));
                lblDesc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                lblDesc.setStyle("-fx-text-alignment: center;");

                // Separador decorativo
                HBox separator = new HBox();
                separator.setPrefWidth(80);
                separator.setPrefHeight(4);
                separator.setStyle("-fx-background-color: #e94560; -fx-background-radius: 2;");
                separator.setMaxWidth(80);

                // Botones
                HBox buttons = new HBox(20);
                buttons.setAlignment(Pos.CENTER);

                Button btnSignIn = new Button("Iniciar Sesión");
                btnSignIn.setPrefWidth(200);
                btnSignIn.setPrefHeight(50);
                btnSignIn.setStyle(
                                "-fx-background-color: #e94560; -fx-text-fill: white; " +
                                                "-fx-font-weight: bold; -fx-font-size: 16; -fx-background-radius: 8;");
                btnSignIn.setOnMouseEntered(e -> btnSignIn.setStyle(
                                "-fx-background-color: #c73652; -fx-text-fill: white; " +
                                                "-fx-font-weight: bold; -fx-font-size: 16; -fx-background-radius: 8;"));
                btnSignIn.setOnMouseExited(e -> btnSignIn.setStyle(
                                "-fx-background-color: #e94560; -fx-text-fill: white; " +
                                                "-fx-font-weight: bold; -fx-font-size: 16; -fx-background-radius: 8;"));
                btnSignIn.setOnAction(e -> mainFrame.showSignIn());

                Button btnSignUp = new Button("Registrarse");
                btnSignUp.setPrefWidth(200);
                btnSignUp.setPrefHeight(50);
                btnSignUp.setStyle(
                                "-fx-background-color: transparent; -fx-text-fill: #e94560; " +
                                                "-fx-font-weight: bold; -fx-font-size: 16; -fx-border-color: #e94560; "
                                                +
                                                "-fx-border-radius: 8; -fx-background-radius: 8;");
                btnSignUp.setOnMouseEntered(e -> btnSignUp.setStyle(
                                "-fx-background-color: #1a1a2e; -fx-text-fill: #e94560; " +
                                                "-fx-font-weight: bold; -fx-font-size: 16; -fx-border-color: #e94560; "
                                                +
                                                "-fx-border-radius: 8; -fx-background-radius: 8;"));
                btnSignUp.setOnMouseExited(e -> btnSignUp.setStyle(
                                "-fx-background-color: transparent; -fx-text-fill: #e94560; " +
                                                "-fx-font-weight: bold; -fx-font-size: 16; -fx-border-color: #e94560; "
                                                +
                                                "-fx-border-radius: 8; -fx-background-radius: 8;"));
                btnSignUp.setOnAction(e -> mainFrame.showSignUp());

                buttons.getChildren().addAll(btnSignIn, btnSignUp);

                center.getChildren().addAll(lblWelcome, separator, lblDesc, buttons);

                // ── Pie ─────────────────────────────────────────────────────────────────
                HBox footer = new HBox();
                footer.setPadding(new Insets(15));
                footer.setAlignment(Pos.CENTER);
                footer.setStyle("-fx-background-color: #1a1a2e;");
                Label lblFooter = new Label("2026 Avanti Videoclub · Team Avanti");
                lblFooter.setTextFill(Color.web("#444466"));
                footer.getChildren().add(lblFooter);

                setTop(header);
                setCenter(center);
                setBottom(footer);
        }
}
