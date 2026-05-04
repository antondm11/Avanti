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

public class MisPeliculasSection extends VBox {

    private VBox panelActivos;
    private VBox panelVencidos;
    private VBox panelHistorial;
    private Label lblCargando;

    private UserPanel userPanel;

    public MisPeliculasSection(UserPanel userPanel) {
        this.userPanel = userPanel;
        createUI();
        loadRentalsFromDb();
    }

    private void createUI() {
        setSpacing(0);
        setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(this, Priority.ALWAYS);

        lblCargando = new Label("Cargando tus alquileres...");
        lblCargando.setTextFill(Color.web("#a0a0a0"));

        // ACTIVOS
        Label lblActivos = new Label("▶  Alquileres Activos");
        lblActivos.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblActivos.setTextFill(Color.web("#4ecdc4"));
        panelActivos = new VBox(10);
        panelActivos.setAlignment(Pos.CENTER);

        // VENCIDOS
        Label lblVencidos = new Label("⚠  Alquileres Vencidos");
        lblVencidos.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblVencidos.setTextFill(Color.web("#ff6b6b"));
        panelVencidos = new VBox(10);
        panelVencidos.setAlignment(Pos.CENTER);

        // HISTORIAL
        Label lblHistorial = new Label("✔  Historial de Devueltas");
        lblHistorial.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblHistorial.setTextFill(Color.web("#a0a0a0"));
        panelHistorial = new VBox(10);
        panelHistorial.setAlignment(Pos.CENTER);

        Label lblTitulo = new Label("MIS PELÍCULAS");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 24));
        lblTitulo.setTextFill(Color.web("#e94560"));

        VBox contenido = new VBox(20,
                lblTitulo, lblCargando,
                lblActivos, panelActivos,
                lblVencidos, panelVencidos,
                lblHistorial, panelHistorial);
        contenido.setAlignment(Pos.TOP_CENTER);
        contenido.setPadding(new Insets(10, 10, 30, 10));

        ScrollPane scroll = new ScrollPane(contenido);
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
        panelActivos.getChildren().clear();
        panelVencidos.getChildren().clear();
        panelHistorial.getChildren().clear();
        lblCargando.setText("Cargando tus alquileres...");
        lblCargando.setTextFill(Color.web("#a0a0a0"));

        if (SessionManager.getCurrentUser() == null) {
            lblCargando.setText("No has iniciado sesión.");
            return;
        }

        int idUsuario = SessionManager.getCurrentUser().getId();

        new Thread(() -> {
            try {
                RentalFunctions rf = new RentalFunctions();

                // Actualizar vencidos UNA SOLA VEZ antes de las tres consultas
                rf.updateExpiredRentals();

                List<Rental> activos   = rf.getActiveRentalsByUser(idUsuario);
                List<Rental> vencidos  = rf.getExpiredRentalsByUser(idUsuario);
                List<Rental> devueltos = rf.getReturnedRentalsByUser(idUsuario);

                Platform.runLater(() -> {
                    lblCargando.setText("");

                    if (activos.isEmpty()) {
                        addEmptyLabel(panelActivos, "No tienes alquileres activos.");
                    } else {
                        for (Rental r : activos) panelActivos.getChildren().add(createRentalCard(r, "ACTIVO", rf));
                    }

                    if (vencidos.isEmpty()) {
                        addEmptyLabel(panelVencidos, "No tienes alquileres vencidos.");
                    } else {
                        for (Rental r : vencidos) panelVencidos.getChildren().add(createRentalCard(r, "VENCIDO", rf));
                    }

                    if (devueltos.isEmpty()) {
                        addEmptyLabel(panelHistorial, "No tienes películas devueltas.");
                    } else {
                        for (Rental r : devueltos) panelHistorial.getChildren().add(createRentalCard(r, "DEVUELTO", rf));
                    }
                });

            } catch (SQLException e) {
                Platform.runLater(() -> {
                    lblCargando.setText("Error al conectar con la base de datos: " + e.getMessage());
                    lblCargando.setTextFill(Color.web("#ff6b6b"));
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

    private HBox createRentalCard(Rental r, String tipo, RentalFunctions rf) {
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

        String titulo = (r.getPelicula() != null && r.getPelicula().getTitulo() != null)
                ? r.getPelicula().getTitulo()
                : "Película #" + r.getIdPelicula();

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblTitulo.setTextFill(Color.WHITE);

        String fechas = "Alquilado: " + r.getFechaAlquiler();
        if (r.getFechaDevolucion() != null) fechas += "   Devolver antes del: " + r.getFechaDevolucion();
        Label lblFecha = new Label(fechas);
        lblFecha.setTextFill(Color.web("#a0a0a0"));
        lblFecha.setFont(Font.font(12));

        Color colorEstado = switch (tipo) {
            case "ACTIVO"   -> Color.web("#4ecdc4");
            case "VENCIDO"  -> Color.web("#ff6b6b");
            default         -> Color.web("#a0a0a0");
        };
        Label lblEstado = new Label("Estado: " + r.getEstado());
        lblEstado.setTextFill(colorEstado);
        lblEstado.setFont(Font.font("System", FontWeight.BOLD, 12));

        Label lblPrecio = new Label(String.format("Precio: %.2f EUR", r.getPrecio()));
        lblPrecio.setTextFill(Color.web("#a0a0a0"));
        lblPrecio.setFont(Font.font(12));

        info.getChildren().addAll(lblTitulo, lblFecha, lblEstado, lblPrecio);

        if (r.getMulta() > 0) {
            Label lblMulta = new Label(String.format("⚠ Multa acumulada: %.2f EUR", r.getMulta()));
            lblMulta.setTextFill(Color.web("#ff6b6b"));
            lblMulta.setFont(Font.font("System", FontWeight.BOLD, 13));
            info.getChildren().add(lblMulta);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // ── Botones ───────────────────────────────────────────────────────────────
        HBox botones = new HBox(10);
        botones.setAlignment(Pos.CENTER_RIGHT);

        if ("ACTIVO".equals(tipo) || "VENCIDO".equals(tipo)) {

            // Botón VER (solo en activos)
            if ("ACTIVO".equals(tipo)) {
                Button btnVer = new Button("▶ VER");
                btnVer.setStyle(
                        "-fx-background-color: #e94560; -fx-text-fill: white;" +
                        " -fx-font-weight: bold; -fx-padding: 10 20;");
                btnVer.setOnMouseEntered(e -> btnVer.setStyle(
                        "-fx-background-color: #c73652; -fx-text-fill: white;" +
                        " -fx-font-weight: bold; -fx-padding: 10 20;"));
                btnVer.setOnMouseExited(e -> btnVer.setStyle(
                        "-fx-background-color: #e94560; -fx-text-fill: white;" +
                        " -fx-font-weight: bold; -fx-padding: 10 20;"));
                btnVer.setOnAction(e -> playMovie(r.getIdPelicula(), titulo));
                botones.getChildren().add(btnVer);
            }

            // Botón DEVOLVER
            Button btnDevolver = new Button("DEVOLVER");
            String colorBtn = "ACTIVO".equals(tipo) ? "#4ecdc4" : "#ff6b6b";
            btnDevolver.setStyle(
                    "-fx-background-color: " + colorBtn + "; -fx-text-fill: #0f0f1a;" +
                    " -fx-font-weight: bold; -fx-padding: 10 20;");
            btnDevolver.setOnAction(e -> returnMovie(r, rf, btnDevolver, titulo));
            botones.getChildren().add(btnDevolver);
        }

        card.getChildren().addAll(info, spacer, botones);
        return card;
    }

    // ── Reproducir vídeo ─────────────────────────────────────────────────────────

    private void playMovie(int idPelicula, String tituloFallback) {
        new Thread(() -> {
            try {
                MovieFunctions mf = new MovieFunctions();
                Movie movie = mf.getMovieById(idPelicula);
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

    private void returnMovie(Rental r, RentalFunctions rf, Button btnDevolver, String titulo) {
        btnDevolver.setDisable(true);
        btnDevolver.setText("Devolviendo...");

        new Thread(() -> {
            try {
                rf.returnMovie(r.getId());
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION,
                            "Has devuelto: " + titulo + "\nGracias por usar Avanti.");
                    loadRentalsFromDb();
                    if (userPanel != null) userPanel.refreshAlquilarSection();
                });
            } catch (SQLException ex) {
                Platform.runLater(() -> {
                    btnDevolver.setDisable(false);
                    btnDevolver.setText("DEVOLVER");
                    showAlert(Alert.AlertType.ERROR,
                            "Error al devolver: " + ex.getMessage());
                });
                ex.printStackTrace();
            }
        }).start();
    }

    // ── Helper ────────────────────────────────────────────────────────────────────

    private void showAlert(Alert.AlertType type, String mensaje) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
