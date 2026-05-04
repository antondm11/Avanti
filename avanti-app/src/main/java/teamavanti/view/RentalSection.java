package teamavanti.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import teamavanti.bbdd.MovieFunctions;
import teamavanti.bbdd.RentalFunctions;
import teamavanti.model.Movie;
import teamavanti.util.SessionManager;

import java.sql.SQLException;
import java.util.List;

public class RentalSection extends VBox {

    private VBox listMovies;
    private Label lblStatus;
    private UserPanel userPanel;

    public RentalSection(UserPanel userPanel) {
        this.userPanel = userPanel;
        createUI();
        loadAvailableMovies();
    }

    private void createUI() {
        setSpacing(15);
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(10));
        VBox.setVgrow(this, Priority.ALWAYS);

        Label lblTitle = new Label("ALQUILAR PELÍCULA");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        lblTitle.setTextFill(Color.web("#e94560"));

        Label lblSub = new Label("Selecciona una película disponible");
        lblSub.setTextFill(Color.web("#a0a0a0"));

        lblStatus = new Label("");
        lblStatus.setTextFill(Color.web("#a0a0a0"));

        listMovies = new VBox(15);
        listMovies.setAlignment(Pos.CENTER);
        listMovies.setPadding(new Insets(5));

        ScrollPane scroll = new ScrollPane(listMovies);
        scroll.setStyle("-fx-background: #0f0f1a; -fx-background-color: transparent;");
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        getChildren().addAll(lblTitle, lblSub, lblStatus, scroll);
    }

    /** Carga (o recarga) las películas disponibles desde la BD. */
    public void loadAvailableMovies() {
        listMovies.getChildren().clear();
        lblStatus.setText("Cargando películas disponibles...");

        new Thread(() -> {
            try {
                MovieFunctions mf = new MovieFunctions();
                List<Movie> availableMovies = mf.getAvailableMovies();

                int idUser = SessionManager.getCurrentUser() != null
                        ? SessionManager.getCurrentUser().getId()
                        : -1;

                RentalFunctions rf = new RentalFunctions();

                Platform.runLater(() -> {
                    listMovies.getChildren().clear();
                    lblStatus.setText("");

                    if (availableMovies.isEmpty()) {
                        Label empty = new Label("No hay películas disponibles en este momento.");
                        empty.setTextFill(Color.web("#a0a0a0"));
                        listMovies.getChildren().add(empty);
                        return;
                    }

                    for (Movie m : availableMovies) {
                        boolean isRented = false;
                        if (idUser != -1) {
                            try {
                                isRented = rf.hasActiveRental(idUser, m.getId());
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        listMovies.getChildren().add(createRentalRow(m, isRented, idUser, rf));
                    }
                });

            } catch (SQLException e) {
                Platform.runLater(() -> {
                    lblStatus.setText("Error al conectar con la base de datos.");
                    lblStatus.setTextFill(Color.web("#ff6b6b"));
                });
                e.printStackTrace();
            }
        }).start();
    }

    private HBox createRentalRow(Movie m, boolean isRented, int idUser, RentalFunctions rf) {
        HBox row = new HBox(20);
        row.setPadding(new Insets(15));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
                "-fx-background-color: #1a1a2e; -fx-background-radius: 8; -fx-border-color: #16213e; -fx-border-radius: 8;");
        row.setMaxWidth(700);

        VBox info = new VBox(5);
        info.setAlignment(Pos.CENTER_LEFT);

        Label lblMovieTitle = new Label(m.getTitulo());
        lblMovieTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblMovieTitle.setTextFill(Color.WHITE);

        Label lblInfo = new Label(m.getDirector() + " · " + m.getAno() + " · " + m.getDuracion() + " min");
        lblInfo.setTextFill(Color.web("#a0a0a0"));

        Label lblGenre = new Label(m.getGenero() != null && !m.getGenero().isEmpty() ? m.getGenero() : "");
        lblGenre.setTextFill(Color.web("#4ecdc4"));
        lblGenre.setFont(Font.font(12));

        Label lblPrice = new Label(String.format("%.2f EUR", m.getPrecio()));
        lblPrice.setFont(Font.font("System", FontWeight.BOLD, 18));
        lblPrice.setTextFill(Color.web("#e94560"));

        info.getChildren().addAll(lblMovieTitle, lblInfo, lblGenre, lblPrice);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRent;
        if (isRented) {
            btnRent = new Button("YA ALQUILADA");

            btnRent.setCursor(javafx.scene.Cursor.HAND);
            btnRent.setStyle(
                    "-fx-background-color: #555577; -fx-text-fill: #a0a0a0; -fx-font-weight: bold; -fx-padding: 10 25;");
            btnRent.setDisable(true);
        } else {
            btnRent = new Button("ALQUILAR");

            btnRent.setCursor(javafx.scene.Cursor.HAND);
            btnRent.setStyle(
                    "-fx-background-color: #4ecdc4; -fx-text-fill: #0f0f1a; -fx-font-weight: bold; -fx-padding: 10 25;");
            btnRent.setOnAction(e -> rentMovie(m, idUser, rf));
        }

        row.getChildren().addAll(info, spacer, btnRent);
        return row;
    }

    private void rentMovie(Movie m, int idUser, RentalFunctions rf) {
        if (idUser == -1) {
            showAlert(Alert.AlertType.ERROR, "No has iniciado sesión.");
            return;
        }

        new Thread(() -> {
            try {
                rf.rentMovie(m.getId(), idUser);
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION,
                            "Has alquilado: " + m.getTitulo() + "\nDisponible durante 7 días.");
                    // Refrescar disponibles
                    loadAvailableMovies();
                    // Refrescar Mis Películas para que aparezca el nuevo alquiler
                    if (userPanel != null)
                        userPanel.refreshMisPeliculasSection();
                });
            } catch (SQLException ex) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR,
                        "Error al registrar el alquiler: " + ex.getMessage()));
                ex.printStackTrace();
            }
        }).start();
    }

    private void showAlert(Alert.AlertType type, String mensaje) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
