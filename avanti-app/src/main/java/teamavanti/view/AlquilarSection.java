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

public class AlquilarSection extends VBox {

    private VBox listaPeliculas;
    private Label lblEstado;
    private UserPanel userPanel;

    public AlquilarSection(UserPanel userPanel) {
        this.userPanel = userPanel;
        createUI();
        loadAvailableMovies();
    }

    private void createUI() {
        setSpacing(15);
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(10));
        VBox.setVgrow(this, Priority.ALWAYS);

        Label lblTitulo = new Label("ALQUILAR PELÍCULA");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 24));
        lblTitulo.setTextFill(Color.web("#e94560"));

        Label lblSub = new Label("Selecciona una película disponible");
        lblSub.setTextFill(Color.web("#a0a0a0"));

        lblEstado = new Label("");
        lblEstado.setTextFill(Color.web("#a0a0a0"));

        listaPeliculas = new VBox(15);
        listaPeliculas.setAlignment(Pos.CENTER);
        listaPeliculas.setPadding(new Insets(5));

        ScrollPane scroll = new ScrollPane(listaPeliculas);
        scroll.setStyle("-fx-background: #0f0f1a; -fx-background-color: transparent;");
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        getChildren().addAll(lblTitulo, lblSub, lblEstado, scroll);
    }

    /** Carga (o recarga) las películas disponibles desde la BD. */
    public void loadAvailableMovies() {
        listaPeliculas.getChildren().clear();
        lblEstado.setText("Cargando películas disponibles...");

        new Thread(() -> {
            try {
                MovieFunctions mf = new MovieFunctions();
                List<Movie> disponibles = mf.getAvailableMovies();

                int idUsuario = SessionManager.getCurrentUser() != null
                        ? SessionManager.getCurrentUser().getId()
                        : -1;

                RentalFunctions rf = new RentalFunctions();

                Platform.runLater(() -> {
                    listaPeliculas.getChildren().clear();
                    lblEstado.setText("");

                    if (disponibles.isEmpty()) {
                        Label vacio = new Label("No hay películas disponibles en este momento.");
                        vacio.setTextFill(Color.web("#a0a0a0"));
                        listaPeliculas.getChildren().add(vacio);
                        return;
                    }

                    for (Movie m : disponibles) {
                        boolean yaAlquilada = false;
                        if (idUsuario != -1) {
                            try {
                                yaAlquilada = rf.hasActiveRental(idUsuario, m.getId());
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        listaPeliculas.getChildren().add(createRentalRow(m, yaAlquilada, idUsuario, rf));
                    }
                });

            } catch (SQLException e) {
                Platform.runLater(() -> {
                    lblEstado.setText("Error al conectar con la base de datos.");
                    lblEstado.setTextFill(Color.web("#ff6b6b"));
                });
                e.printStackTrace();
            }
        }).start();
    }

    private HBox createRentalRow(Movie m, boolean yaAlquilada, int idUsuario, RentalFunctions rf) {
        HBox row = new HBox(20);
        row.setPadding(new Insets(15));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
                "-fx-background-color: #1a1a2e; -fx-background-radius: 8; -fx-border-color: #16213e; -fx-border-radius: 8;");
        row.setMaxWidth(700);

        VBox info = new VBox(5);
        info.setAlignment(Pos.CENTER_LEFT);

        Label lblTitulo = new Label(m.getTitulo());
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblTitulo.setTextFill(Color.WHITE);

        Label lblDetalles = new Label(m.getDirector() + " · " + m.getAno() + " · " + m.getDuracion() + " min");
        lblDetalles.setTextFill(Color.web("#a0a0a0"));

        Label lblGenero = new Label(m.getGenero() != null && !m.getGenero().isEmpty() ? m.getGenero() : "");
        lblGenero.setTextFill(Color.web("#4ecdc4"));
        lblGenero.setFont(Font.font(12));

        Label lblPrecio = new Label(String.format("%.2f EUR", m.getPrecio()));
        lblPrecio.setFont(Font.font("System", FontWeight.BOLD, 18));
        lblPrecio.setTextFill(Color.web("#e94560"));

        info.getChildren().addAll(lblTitulo, lblDetalles, lblGenero, lblPrecio);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAlquilar;
        if (yaAlquilada) {
            btnAlquilar = new Button("YA ALQUILADA");
            btnAlquilar.setStyle(
                    "-fx-background-color: #555577; -fx-text-fill: #a0a0a0; -fx-font-weight: bold; -fx-padding: 10 25;");
            btnAlquilar.setDisable(true);
        } else {
            btnAlquilar = new Button("ALQUILAR");
            btnAlquilar.setStyle(
                    "-fx-background-color: #4ecdc4; -fx-text-fill: #0f0f1a; -fx-font-weight: bold; -fx-padding: 10 25;");
            btnAlquilar.setOnAction(e -> rentMovie(m, idUsuario, rf));
        }

        row.getChildren().addAll(info, spacer, btnAlquilar);
        return row;
    }

    private void rentMovie(Movie m, int idUsuario, RentalFunctions rf) {
        if (idUsuario == -1) {
            showAlert(Alert.AlertType.ERROR, "No has iniciado sesión.");
            return;
        }

        new Thread(() -> {
            try {
                rf.rentMovie(m.getId(), idUsuario);
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION,
                            "Has alquilado: " + m.getTitulo() + "\nDisponible durante 7 días.");
                    // Refrescar disponibles
                    loadAvailableMovies();
                    // Refrescar Mis Películas para que aparezca el nuevo alquiler
                    if (userPanel != null) userPanel.refreshMisPeliculasSection();
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
