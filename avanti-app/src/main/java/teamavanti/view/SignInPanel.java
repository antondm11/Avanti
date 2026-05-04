package teamavanti.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import teamavanti.model.User;
import teamavanti.util.SessionManager;
import teamavanti.bbdd.UserFunctions;
import java.sql.SQLException;

/**
 * Pantalla de inicio de sesión.
 */
public class SignInPanel extends BorderPane {

    private MainFrame mainFrame;
    private TextField txtEmail;
    private PasswordField txtPassword;
    private Label lblError;

    public SignInPanel(MainFrame mainFrame) {
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
        lblLogo.setFont(Font.font("System", FontWeight.BOLD, 24));
        lblLogo.setTextFill(Color.web("#e94560"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnBack = new Button("← Volver");
        btnBack.setCursor(javafx.scene.Cursor.HAND);
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #a0a0a0;");
        btnBack.setOnAction(e -> mainFrame.showHome());

        header.getChildren().addAll(lblLogo, spacer, btnBack);

        // ── Formulario central ──────────────────────────────────────────────────
        VBox form = new VBox(18);
        form.setAlignment(Pos.CENTER);
        form.setMaxWidth(400);
        form.setPadding(new Insets(40));
        form.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 12;");

        Label lblTitle = new Label("Iniciar Sesión");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 28));
        lblTitle.setTextFill(Color.WHITE);

        Label lblSub = new Label("Accede a tu cuenta de Avanti");
        lblSub.setTextFill(Color.web("#a0a0a0"));

        String inputStyle = "-fx-background-color: #16213e; -fx-text-fill: white; " +
                "-fx-prompt-text-fill: #555577; -fx-border-color: #2a2a4a; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 10;";

        txtEmail = new TextField();
        txtEmail.setPromptText("Correo electrónico");
        txtEmail.setStyle(inputStyle);
        txtEmail.setPrefHeight(42);

        txtPassword = new PasswordField();
        txtPassword.setPromptText("Contraseña");
        txtPassword.setStyle(inputStyle);
        txtPassword.setPrefHeight(42);
        txtPassword.setOnAction(e -> handleLogin());

        lblError = new Label("");
        lblError.setTextFill(Color.web("#ff6b6b"));
        lblError.setFont(Font.font(12));

        Button btnLogin = new Button("ENTRAR");
        btnLogin.setCursor(javafx.scene.Cursor.HAND);
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setPrefHeight(45);
        btnLogin.setStyle(
                "-fx-background-color: #e94560; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8;");
        btnLogin.setOnAction(e -> handleLogin());

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #2a2a4a;");

        Label lblRegister = new Label("No tienes cuenta?");
        lblRegister.setTextFill(Color.web("#a0a0a0"));

        Button btnRegister = new Button("Regístrate gratis");
        btnRegister.setCursor(javafx.scene.Cursor.HAND);
        btnRegister.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #4ecdc4; " +
                        "-fx-underline: true;");
        btnRegister.setOnAction(e -> mainFrame.showSignUp());

        HBox linkBox = new HBox(6, lblRegister, btnRegister);
        linkBox.setAlignment(Pos.CENTER);

        form.getChildren().addAll(
                lblTitle, lblSub, txtEmail, txtPassword, lblError, btnLogin, sep, linkBox);

        // Centrar el formulario en la pantalla
        StackPane center = new StackPane(form);
        center.setStyle("-fx-background-color: #0f0f1a;");

        setTop(header);
        setCenter(center);
    }

    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            lblError.setText("Por favor, rellena todos los campos.");
            return;
        }

        try {
            UserFunctions uf = new UserFunctions();
            User user = uf.loginUser(email, password);

            if (user != null) {
                SessionManager.setCurrentUser(user);
                mainFrame.showLoggingInPanel();
            } else {
                lblError.setText("Correo o contraseña incorrectos.");
            }
        } catch (SQLException ex) {
            lblError.setText("Error al conectar con la base de datos.");
            ex.printStackTrace();
        }
    }
}
