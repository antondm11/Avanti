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
import teamavanti.model.Rental;
import teamavanti.util.SessionManager;

import java.awt.Desktop;
import java.net.URI;
import java.sql.SQLException;
import java.util.List;

public class MyMoviesSection extends VBox {

    private VBox panelActive;
    private VBox panelExpired;
    private VBox panelHistory;
    private Label lblLoading;

    private UserPanel userPanel;

    public MyMoviesSection(UserPanel userPanel) {
        this.userPanel = userPanel;
        createUI();
        loadRentalsFromDb();
    }

    private void createUI() {
        setSpacing(0);
        setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(this, Priority.ALWAYS);

        lblLoading = new Label("Cargando tus alquileres...");
        lblLoading.setTextFill(Color.web("#a0a0a0"));

        // ACTIVOS
        Label lblActive = new Label("▶  Alquileres Activos");
        lblActive.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblActive.setTextFill(Color.web("#4ecdc4"));
        panelActive = new VBox(10);
        panelActive.setAlignment(Pos.CENTER);

        // VENCIDOS
        Label lblExpired = new Label("⚠  Alquileres Vencidos");
        lblExpired.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblExpired.setTextFill(Color.web("#ff6b6b"));
        panelExpired = new VBox(10);
        panelExpired.setAlignment(Pos.CENTER);

        // HISTORIAL
        Label lblHistory = new Label("✔  Historial de Devueltas");
        lblHistory.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblHistory.setTextFill(Color.web("#a0a0a0"));
        panelHistory = new VBox(10);
        panelHistory.setAlignment(Pos.CENTER);

        Label lblTitle = new Label("MIS PELÍCULAS");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        lblTitle.setTextFill(Color.web("#e94560"));

        VBox content = new VBox(20,
                lblTitle, lblLoading,
                lblActive, panelActive,
                lblExpired, panelExpired,
                lblHistory, panelHistory);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(10, 10, 30, 10));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setStyle("-fx-background: #0f0f1a; -fx-background-color: transparent;");
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        getChildren().add(scroll);
    }

    /** Carga (o recarga) los alquileres del usuario desde la BD. */
    public void loadRentalsFromDb() {
        panelActive.getChildren().clear();
        panelExpired.getChildren().clear();
        panelHistory.getChildren().clear();
        lblLoading.setText("Cargando tus alquileres...");
        lblLoading.setTextFill(Color.web("#a0a0a0"));

        if (SessionManager.getCurrentUser() == null) {
            lblLoading.setText("No has iniciado sesión.");
            return;
        }

        int idUser = SessionManager.getCurrentUser().getId();

        new Thread(() -> {
            try {
                RentalFunctions rf = new RentalFunctions();

                // Actualizar vencidos UNA SOLA VEZ antes de las tres consultas
                rf.updateExpiredRentals();

                List<Rental> active = rf.getActiveRentalsByUser(idUser);
                List<Rental> expired = rf.getExpiredRentalsByUser(idUser);
                List<Rental> returned = rf.getReturnedRentalsByUser(idUser);

                Platform.runLater(() -> {
                    lblLoading.setText("");

                    if (active.isEmpty()) {
                        addEmptyLabel(panelActive, "No tienes alquileres activos.");
                    } else {
                        for (Rental r : active)
                            panelActive.getChildren().add(createRentalCard(r, "ACTIVO", rf));
                    }

                    if (expired.isEmpty()) {
                        addEmptyLabel(panelExpired, "No tienes alquileres vencidos.");
                    } else {
                        for (Rental r : expired)
                            panelExpired.getChildren().add(createRentalCard(r, "VENCIDO", rf));
                    }

                    if (returned.isEmpty()) {
                        addEmptyLabel(panelHistory, "No tienes películas devueltas.");
                    } else {
                        for (Rental r : returned)
                            panelHistory.getChildren().add(createRentalCard(r, "DEVUELTO", rf));
                    }
                });

            } catch (SQLException e) {
                Platform.runLater(() -> {
                    lblLoading.setText("Error al conectar con la base de datos: " + e.getMessage());
                    lblLoading.setTextFill(Color.web("#ff6b6b"));
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void addEmptyLabel(VBox panel, String texto) {
        Label lbl = new Label(texto);
        lbl.setTextFill(Color.web("#555577"));
        lbl.setFont(Font.font(13));
        panel.getChildren().add(lbl);
    }

    private HBox createRentalCard(Rental r, String type, RentalFunctions rf) {
        HBox card = new HBox(20);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: #1a1a2e; -fx-background-radius: 8;" +
                        " -fx-border-color: #16213e; -fx-border-radius: 8;");
        card.setMaxWidth(720);

        // ── Info ─────────────────────────────────────────────────────────────────
        VBox info = new VBox(5);
        info.setAlignment(Pos.CENTER_LEFT);

        String movieTitle = (r.getPelicula() != null && r.getPelicula().getTitulo() != null)
                ? r.getPelicula().getTitulo()
                : "Película #" + r.getIdPelicula();

        Label lblTitle = new Label(movieTitle);
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblTitle.setTextFill(Color.WHITE);

        String dates = "Alquilado: " + r.getFechaAlquiler();
        if (r.getFechaDevolucion() != null)
            dates += "   Devolver antes del: " + r.getFechaDevolucion();
        Label lblDates = new Label(dates);
        lblDates.setTextFill(Color.web("#a0a0a0"));
        lblDates.setFont(Font.font(12));

        Color colorStatus = switch (type) {
            case "ACTIVO" -> Color.web("#4ecdc4");
            case "VENCIDO" -> Color.web("#ff6b6b");
            default -> Color.web("#a0a0a0");
        };
        Label lblStatus = new Label("Estado: " + r.getEstado());
        lblStatus.setTextFill(colorStatus);
        lblStatus.setFont(Font.font("System", FontWeight.BOLD, 12));

        Label lblPrice = new Label(String.format("Precio: %.2f EUR", r.getPrecio()));
        lblPrice.setTextFill(Color.web("#a0a0a0"));
        lblPrice.setFont(Font.font(12));

        info.getChildren().addAll(lblTitle, lblDates, lblStatus, lblPrice);

        if (r.getMulta() > 0) {
            Label lblFine = new Label(String.format("⚠ Multa acumulada: %.2f EUR", r.getMulta()));
            lblFine.setTextFill(Color.web("#ff6b6b"));
            lblFine.setFont(Font.font("System", FontWeight.BOLD, 13));
            info.getChildren().add(lblFine);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // ── Botones ───────────────────────────────────────────────────────────────
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        if ("ACTIVO".equals(type) || "VENCIDO".equals(type)) {

            // Botón VER (solo en activos)
            if ("ACTIVO".equals(type)) {
                Button btnCheck = new Button("▶ VER");
                btnCheck.setStyle(
                        "-fx-background-color: #e94560; -fx-text-fill: white;" +
                                " -fx-font-weight: bold; -fx-padding: 10 20;");
                btnCheck.setOnMouseEntered(e -> btnCheck.setStyle(
                        "-fx-background-color: #c73652; -fx-text-fill: white;" +
                                " -fx-font-weight: bold; -fx-padding: 10 20;"));
                btnCheck.setOnMouseExited(e -> btnCheck.setStyle(
                        "-fx-background-color: #e94560; -fx-text-fill: white;" +
                                " -fx-font-weight: bold; -fx-padding: 10 20;"));
                btnCheck.setOnAction(e -> playMovie(r.getIdPelicula(), movieTitle));
                buttons.getChildren().add(btnCheck);
            }

            // Botón DEVOLVER
            Button btnReturn = new Button("DEVOLVER");
            String colorBtn = "ACTIVO".equals(type) ? "#4ecdc4" : "#ff6b6b";
            btnReturn.setStyle(
                    "-fx-background-color: " + colorBtn + "; -fx-text-fill: #0f0f1a;" +
                            " -fx-font-weight: bold; -fx-padding: 10 20;");
            btnReturn.setOnAction(e -> returnMovie(r, rf, btnReturn, movieTitle));
            buttons.getChildren().add(btnReturn);
        }

        card.getChildren().addAll(info, spacer, buttons);
        return card;
    }

    // ── Reproducir vídeo ─────────────────────────────────────────────────────────

    private void playMovie(int idMovie, String titleFallback) {
        new Thread(() -> {
            try {
                MovieFunctions mf = new MovieFunctions();
                Movie movie = mf.getMovieById(idMovie);
                Platform.runLater(() -> {
                    if (movie != null) {
                        String url = movie.getVideo();
                        if (url != null && !url.isBlank()
                                && !url.equalsIgnoreCase("url_video")
                                && (url.startsWith("http://") || url.startsWith("https://"))) {
                            // Intentar abrir en el navegador
                            try {
                                if (Desktop.isDesktopSupported()) {
                                    Desktop.getDesktop().browse(new URI(url));
                                } else {
                                    showAlert(Alert.AlertType.INFORMATION,
                                            "URL del vídeo:\n" + url);
                                }
                            } catch (Exception ex) {
                                showAlert(Alert.AlertType.ERROR,
                                        "No se pudo abrir el vídeo:\n" + ex.getMessage());
                            }
                        } else {
                            // URL placeholder — mostrar diálogo informativo
                            showAlert(Alert.AlertType.INFORMATION,
                                    "▶ Reproduciendo: " + movie.getTitulo()
                                            + "\n\nEsta es una demo. No hay vídeo disponible para esta película.");
                        }
                    } else {
                        showAlert(Alert.AlertType.WARNING,
                                "No se encontró información para esta película.");
                    }
                });
            } catch (SQLException ex) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR,
                        "Error al cargar los datos de la película."));
                ex.printStackTrace();
            }
        }).start();
    }

    // ── Devolver película ────────────────────────────────────────────────────────

    private void returnMovie(Rental r, RentalFunctions rf, Button btnReturn, String title) {
        btnReturn.setDisable(true);
        btnReturn.setText("Devolviendo...");

        new Thread(() -> {
            try {
                rf.returnMovie(r.getId());
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION,
                            "Has devuelto: " + title + "\nGracias por usar Avanti.");
                    loadRentalsFromDb();
                    if (userPanel != null)
                        userPanel.refreshRentalSection();
                });
            } catch (SQLException ex) {
                Platform.runLater(() -> {
                    btnReturn.setDisable(false);
                    btnReturn.setText("DEVOLVER");
                    showAlert(Alert.AlertType.ERROR,
                            "Error al devolver: " + ex.getMessage());
                });
                ex.printStackTrace();
            }
        }).start();
    }

    // ── Helper
    // ────────────────────────────────────────────────────────────────────

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
