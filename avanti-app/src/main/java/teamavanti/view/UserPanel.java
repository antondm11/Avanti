package teamavanti.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import teamavanti.util.SessionManager;

public class UserPanel extends BorderPane {

    private MainFrame mainFrame;
    private VBox centralPanel;

    // Las 3 secciones
    private CatalogoSection catalogoSection;
    private AlquilarSection alquilarSection;
    private MisPeliculasSection misPeliculasSection;

    private Button btnCatalogo, btnAlquilar, btnMisPelis;

    public UserPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        catalogoSection = new CatalogoSection();
        alquilarSection = new AlquilarSection(this); // pasa referencia para cross-refresh
        misPeliculasSection = new MisPeliculasSection(this); // pasa referencia para cross-refresh

        createUI();
        showCatalogo(); // Por defecto
    }

    private void createUI() {
        setStyle("-fx-background-color: #0f0f1a;");

        // === BARRA SUPERIOR ===
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 25, 15, 25));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #e94560; -fx-border-width: 0 0 2 0;");

        Label lblLogo = new Label("AVANTI");
        lblLogo.setFont(Font.font("System", FontWeight.BOLD, 24));
        lblLogo.setTextFill(Color.web("#e94560"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblUsuario = new Label("Hola, " +
                (SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getNombre() : "Cliente"));
        lblUsuario.setTextFill(Color.web("#a0a0a0"));

        Button btnLogout = new Button("Cerrar sesión");
        btnLogout.setStyle("-fx-background-color: transparent; -fx-text-fill: #e94560; -fx-border-color: #e94560;");
        btnLogout.setOnAction(e -> mainFrame.logout());

        topBar.getChildren().addAll(lblLogo, spacer, lblUsuario, btnLogout);

        // === BARRA LATERAL ===
        VBox navBar = new VBox(10);
        navBar.setPadding(new Insets(20, 15, 20, 15));
        navBar.setStyle("-fx-background-color: #16213e;");
        navBar.setPrefWidth(180);
        navBar.setAlignment(Pos.TOP_CENTER);

        Label lblMenu = new Label("MENU");
        lblMenu.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblMenu.setTextFill(Color.web("#a0a0a0"));

        btnCatalogo = createNavButton("Catalogo");
        btnCatalogo.setOnAction(e -> showCatalogo());

        btnAlquilar = createNavButton("Alquilar");
        btnAlquilar.setOnAction(e -> showAlquilar());

        btnMisPelis = createNavButton("Mis Películas");
        btnMisPelis.setOnAction(e -> showMisPeliculas());

        navBar.getChildren().addAll(lblMenu, btnCatalogo, btnAlquilar, btnMisPelis);

        // === PANEL CENTRAL ===
        centralPanel = new VBox();
        centralPanel.setPadding(new Insets(15));
        centralPanel.setAlignment(Pos.TOP_CENTER);
        centralPanel.setStyle("-fx-background-color: #0f0f1a;");
        // Necesario para que las secciones internas con ScrollPane ocupen toda la altura
        VBox.setVgrow(centralPanel, Priority.ALWAYS);

        setTop(topBar);
        setLeft(navBar);
        setCenter(centralPanel);
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #a0a0a0; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #e94560; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #a0a0a0; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15;"));
        return btn;
    }

    private void resetButtons() {
        String base = "-fx-background-color: transparent; -fx-text-fill: #a0a0a0; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15;";
        btnCatalogo.setStyle(base);
        btnAlquilar.setStyle(base);
        btnMisPelis.setStyle(base);
    }

    private void setActive(Button btn) {
        btn.setStyle(
                "-fx-background-color: #e94560; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15;");
    }

    public void showCatalogo() {
        resetButtons();
        setActive(btnCatalogo);
        VBox.setVgrow(catalogoSection, Priority.ALWAYS);
        centralPanel.getChildren().setAll(catalogoSection);
    }

    public void showAlquilar() {
        resetButtons();
        setActive(btnAlquilar);
        VBox.setVgrow(alquilarSection, Priority.ALWAYS);
        centralPanel.getChildren().setAll(alquilarSection);
    }

    public void showMisPeliculas() {
        resetButtons();
        setActive(btnMisPelis);
        VBox.setVgrow(misPeliculasSection, Priority.ALWAYS);
        // Siempre recargar para reflejar alquileres recién realizados
        misPeliculasSection.loadRentalsFromDb();
        centralPanel.getChildren().setAll(misPeliculasSection);
    }

    /** Recarga la lista de películas disponibles para alquilar. */
    public void refreshAlquilarSection() {
        alquilarSection.loadAvailableMovies();
    }

    /** Recarga la sección de Mis Películas (llamado tras alquilar). */
    public void refreshMisPeliculasSection() {
        misPeliculasSection.loadRentalsFromDb();
    }
}
