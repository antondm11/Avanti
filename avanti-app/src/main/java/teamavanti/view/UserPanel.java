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

    // Las 3 secciones principales
    private CatalogSection catalogSection;
    private RentalSection rentalSection;
    private MyMoviesSection myMoviesSection;

    private Button btnCatalog;
    private Button btnRental;
    private Button btnMyMovies;

    public UserPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        catalogSection = new CatalogSection();
        rentalSection = new RentalSection(this);
        myMoviesSection = new MyMoviesSection(this);

        createUI();
        showCatalog(); // Por defecto
    }

    private void createUI() {
        setStyle("-fx-background-color: #0f0f1a;");

        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 25, 15, 25));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #e94560; -fx-border-width: 0 0 2 0;");

        Label lblLogo = new Label("AVANTI");
        lblLogo.setFont(Font.font("System", FontWeight.BOLD, 24));
        lblLogo.setTextFill(Color.web("#e94560"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblUser = new Label("Hola, " +
                (SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getNombre() : "Cliente"));
        lblUser.setTextFill(Color.web("#a0a0a0"));

        Button btnLogout = new Button("Cerrar sesión");
        btnLogout.setCursor(javafx.scene.Cursor.HAND);
        btnLogout.setStyle("-fx-background-color: transparent; -fx-text-fill: #e94560; -fx-border-color: #e94560;");
        btnLogout.setOnAction(e -> mainFrame.logout());

        topBar.getChildren().addAll(lblLogo, spacer, lblUser, btnLogout);

        VBox navBar = new VBox(10);
        navBar.setPadding(new Insets(20, 15, 20, 15));
        navBar.setStyle("-fx-background-color: #16213e;");
        navBar.setPrefWidth(180);
        navBar.setAlignment(Pos.TOP_CENTER);

        Label lblMenu = new Label("MENU");
        lblMenu.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblMenu.setTextFill(Color.web("#a0a0a0"));

        btnCatalog = createNavButton("Catálogo");
        btnCatalog.setOnAction(e -> showCatalog());

        btnRental = createNavButton("Alquilar");
        btnRental.setOnAction(e -> showRental());

        btnMyMovies = createNavButton("Mis Películas");
        btnMyMovies.setOnAction(e -> showMyMovies());

        navBar.getChildren().addAll(lblMenu, btnCatalog, btnRental, btnMyMovies);

        centralPanel = new VBox();
        centralPanel.setPadding(new Insets(15));
        centralPanel.setAlignment(Pos.TOP_CENTER);
        centralPanel.setStyle("-fx-background-color: #0f0f1a;");
        VBox.setVgrow(centralPanel, Priority.ALWAYS);

        setTop(topBar);
        setLeft(navBar);
        setCenter(centralPanel);
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setMaxWidth(Double.MAX_VALUE);

        String baseStyle = "-fx-background-color: transparent; -fx-text-fill: #a0a0a0; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15;";
        String hoverStyle = "-fx-background-color: #e94560; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15;";

        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));

        return btn;
    }

    private void resetButtons() {
        String base = "-fx-background-color: transparent; -fx-text-fill: #a0a0a0; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15;";
        btnCatalog.setStyle(base);
        btnRental.setStyle(base);
        btnMyMovies.setStyle(base);
    }

    private void setActive(Button btn) {
        btn.setStyle(
                "-fx-background-color: #e94560; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15;");
    }

    public void showCatalog() {
        resetButtons();
        setActive(btnCatalog);

        VBox.setVgrow(catalogSection, Priority.ALWAYS);
        centralPanel.getChildren().setAll(catalogSection);
    }

    public void showRental() {
        resetButtons();
        setActive(btnRental);

        // Se recarga por si una película ha sido alquilada o devuelta.
        rentalSection.loadAvailableMovies();

        VBox.setVgrow(rentalSection, Priority.ALWAYS);
        centralPanel.getChildren().setAll(rentalSection);
    }

    public void showMyMovies() {
        resetButtons();
        setActive(btnMyMovies);

        // Siempre recargar para reflejar alquileres recién realizados.
        myMoviesSection.loadRentalsFromDb();

        VBox.setVgrow(myMoviesSection, Priority.ALWAYS);
        centralPanel.getChildren().setAll(myMoviesSection);
    }

    /*
     * Alias por compatibilidad con código anterior.
     * Si alguna clase llama todavía a showMisPeliculas(), no se rompe.
     */
    public void showMisPeliculas() {
        showMyMovies();
    }

    /** Recarga la lista de películas disponibles para alquilar. */
    public void refreshRentalSection() {
        rentalSection.loadAvailableMovies();
    }

    /*
     * Alias por compatibilidad con código anterior.
     * Si alguna clase llama todavía a refreshAlquilarSection(), no se rompe.
     */
    public void refreshAlquilarSection() {
        refreshRentalSection();
    }

    /** Recarga la sección de Mis Películas tras alquilar o devolver. */
    public void refreshMisPeliculasSection() {
        myMoviesSection.loadRentalsFromDb();
    }
}