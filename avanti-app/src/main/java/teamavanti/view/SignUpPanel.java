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
 * Pantalla de registro de nuevo usuario.
 */
public class SignUpPanel extends BorderPane {

    private MainFrame mainFrame;
    private TextField txtName;
    private TextField txtEmail;
    private PasswordField txtPassword;
    private PasswordField txtConfirm;
    private Label lblError;

    public SignUpPanel(MainFrame mainFrame) {
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
        VBox form = new VBox(15);
        form.setAlignment(Pos.CENTER);
        form.setMaxWidth(420);
        form.setPadding(new Insets(40));
        form.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 12;");

        Label lblTitle = new Label("Crear Cuenta");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 28));
        lblTitle.setTextFill(Color.WHITE);

        Label lblSub = new Label("Unete a Avanti y empieza a alquilar");
        lblSub.setTextFill(Color.web("#a0a0a0"));

        String inputStyle = "-fx-background-color: #16213e; -fx-text-fill: white; " +
                "-fx-prompt-text-fill: #555577; -fx-border-color: #2a2a4a; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 10;";

        txtName = new TextField();
        txtName.setPromptText("Nombre completo");
        txtName.setStyle(inputStyle);
        txtName.setPrefHeight(42);

        txtEmail = new TextField();
        txtEmail.setPromptText("Correo electrónico");
        txtEmail.setStyle(inputStyle);
        txtEmail.setPrefHeight(42);

        txtPassword = new PasswordField();
        txtPassword.setPromptText("Contraseña");
        txtPassword.setStyle(inputStyle);
        txtPassword.setPrefHeight(42);

        txtConfirm = new PasswordField();
        txtConfirm.setPromptText("Confirmar contraseña");
        txtConfirm.setStyle(inputStyle);
        txtConfirm.setPrefHeight(42);

        lblError = new Label("");
        lblError.setTextFill(Color.web("#ff6b6b"));
        lblError.setFont(Font.font(12));
        lblError.setWrapText(true);

        Button btnRegister = new Button("CREAR CUENTA");
        btnRegister.setCursor(javafx.scene.Cursor.HAND);
        btnRegister.setMaxWidth(Double.MAX_VALUE);
        btnRegister.setPrefHeight(45);
        btnRegister.setStyle(
                "-fx-background-color: #4ecdc4; -fx-text-fill: #0f0f1a; " +
                        "-fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8;");
        btnRegister.setOnAction(e -> handleRegister());

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #2a2a4a;");

        Label lblLogin = new Label("Ya tienes cuenta?");
        lblLogin.setTextFill(Color.web("#a0a0a0"));

        Button btnIrLogin = new Button("Inicia sesión");
        btnIrLogin.setCursor(javafx.scene.Cursor.HAND);
        btnIrLogin.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #e94560; " +
                        "-fx-underline: true;");
        btnIrLogin.setOnAction(e -> mainFrame.showSignIn());

        HBox linkBox = new HBox(6, lblLogin, btnIrLogin);
        linkBox.setAlignment(Pos.CENTER);

        form.getChildren().addAll(
                lblTitle, lblSub,
                txtName, txtEmail, txtPassword, txtConfirm,
                lblError, btnRegister, sep, linkBox);

        StackPane center = new StackPane(form);
        center.setStyle("-fx-background-color: #0f0f1a;");

        setTop(header);
        setCenter(center);
    }

    private void handleRegister() {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();
        String confirm = txtConfirm.getText();

        // Validaciones basicas
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            lblError.setText("Por favor, rellena todos los campos.");
            return;
        }
        if (!email.contains("@")) {
            lblError.setText("Introduce un correo electrónico válido.");
            return;
        }
        if (password.length() < 4) {
            lblError.setText("La contraseña debe tener al menos 4 caracteres.");
            return;
        }
        if (!password.equals(confirm)) {
            lblError.setText("Las contraseñas no coinciden.");
            return;
        }

        User newUser = new User();
        newUser.setNombre(name);
        newUser.setEmail(email);
        newUser.setContrasena(password);
        newUser.setRol("cliente");

        try {
            UserFunctions uf = new UserFunctions();
            uf.registerUser(newUser);

            // Asignar el nuevo usuario a la sesion y entrar al panel
            SessionManager.setCurrentUser(newUser);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registro completado");
            alert.setHeaderText(null);
            alert.setContentText("Bienvenido a Avanti, " + name + "!");
            alert.showAndWait();

            // Mostrar el panel de inicio de sesión también al registrarse
            mainFrame.showLoggingInPanel();
        } catch (SQLException ex) {
            lblError.setText("Error al guardar en la base de datos (quizá el correo ya existe).");
            ex.printStackTrace();
        }
    }
}
