package teamavanti.view;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import teamavanti.bbdd.MovieFunctions;
import teamavanti.bbdd.RentalFunctions;
import teamavanti.model.Movie;
import teamavanti.model.Rental;
import teamavanti.util.SessionManager;

public class MyMoviesSection extends VBox {

    private VBox panelActivos;
    private VBox panelVencidos;
    private VBox panelHistorial;
    private Label lblCargando;

    private UserPanel userPanel;

    public MyMoviesSection(UserPanel userPanel) {
        this.userPanel = userPanel;
        createUI();
        loadRentalsFromDb();
    }

    public MyMoviesSection() {
        this(null);
    }

    private void createUI() {
        setSpacing(25);
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #0f0f1a;");
        VBox.setVgrow(this, Priority.ALWAYS);

        Label lblTitulo = new Label("MIS PELÍCULAS");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 28));
        lblTitulo.setTextFill(Color.WHITE);

        lblCargando = new Label("");
        lblCargando.setTextFill(Color.web("#a0a0a0"));

        Label lblActivos = createSectionTitle("Alquileres Activos", "#4ecdc4");
        panelActivos = createSectionPanel();

        Label lblVencidos = createSectionTitle("Alquileres Vencidos", "#ff6b6b");
        panelVencidos = createSectionPanel();

        Label lblHistorial = createSectionTitle("Historial de Devueltas", "#a0a0a0");
        panelHistorial = createSectionPanel();

        VBox content = new VBox(25);
        content.setAlignment(Pos.TOP_CENTER);
        content.getChildren().addAll(
                lblTitulo,
                lblCargando,
                lblActivos,
                panelActivos,
                lblVencidos,
                panelVencidos,
                lblHistorial,
                panelHistorial);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0f0f1a; -fx-background-color: transparent;");
        scroll.setPrefHeight(520);

        getChildren().add(scroll);
    }

    private Label createSectionTitle(String text, String color) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 20));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    private VBox createSectionPanel() {
        VBox panel = new VBox(12);
        panel.setAlignment(Pos.CENTER);
        panel.setMaxWidth(850);
        return panel;
    }

    public void loadRentalsFromDb() {
        panelActivos.getChildren().clear();
        panelVencidos.getChildren().clear();
        panelHistorial.getChildren().clear();

        if (SessionManager.getCurrentUser() == null) {
            lblCargando.setText("");
            showEmptyMessage(panelActivos, "No hay ningún usuario conectado.");
            return;
        }

        int idUsuario = SessionManager.getCurrentUser().getId();
        lblCargando.setText("Cargando tus películas...");

        new Thread(() -> {
            try {
                RentalFunctions rentalFunctions = new RentalFunctions();

                List<Rental> activos = rentalFunctions.getActiveRentalsByUser(idUsuario);
                List<Rental> vencidos = rentalFunctions.getExpiredRentalsByUser(idUsuario);
                List<Rental> devueltos = rentalFunctions.getReturnedRentalsByUser(idUsuario);

                Platform.runLater(() -> {
                    lblCargando.setText("");

                    fillSection(panelActivos, activos, "No tienes alquileres activos.", "ACTIVO");
                    fillSection(panelVencidos, vencidos, "No tienes alquileres vencidos.", "VENCIDO");
                    fillSection(panelHistorial, devueltos, "Todavía no tienes historial de películas devueltas.",
                            "DEVUELTO");
                });

            } catch (SQLException e) {
                Platform.runLater(() -> {
                    lblCargando.setText("");
                    showAlert(Alert.AlertType.ERROR, "Error al cargar tus alquileres desde la base de datos.");
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void fillSection(VBox panel, List<Rental> rentals, String emptyMessage, String tipo) {
        panel.getChildren().clear();

        if (rentals == null || rentals.isEmpty()) {
            showEmptyMessage(panel, emptyMessage);
            return;
        }

        for (Rental rental : rentals) {
            panel.getChildren().add(createRentalCard(rental, tipo));
        }
    }

    private void showEmptyMessage(VBox panel, String message) {
        Label lbl = new Label(message);
        lbl.setTextFill(Color.web("#f4f4f4"));
        lbl.setFont(Font.font(14));
        panel.getChildren().add(lbl);
    }

    private HBox createRentalCard(Rental rental, String tipo) {
        Movie movie = loadMovieSafely(rental.getIdPelicula());

        HBox card = new HBox(20);
        card.setPadding(new Insets(18, 22, 18, 22));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(850);
        card.setStyle(
                "-fx-background-color: #1a1a2e;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #16213e;" +
                        "-fx-border-radius: 10;");

        VBox info = new VBox(7);
        info.setAlignment(Pos.CENTER_LEFT);

        String titulo = movie != null ? movie.getTitulo() : "Película #" + rental.getIdPelicula();

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 18));
        lblTitulo.setTextFill(Color.WHITE);

        Label lblFechas = new Label(createFechaText(rental, tipo));
        lblFechas.setTextFill(Color.WHITE);
        lblFechas.setFont(Font.font(13));

        Label lblEstado = new Label("Estado: " + rental.getEstado());
        lblEstado.setFont(Font.font("System", FontWeight.BOLD, 13));
        lblEstado.setTextFill(getEstadoColor(rental.getEstado()));

        Label lblPrecio = new Label(String.format("Precio: %.2f EUR", rental.getPrecio()));
        lblPrecio.setTextFill(Color.WHITE);
        lblPrecio.setFont(Font.font(13));

        info.getChildren().addAll(lblTitulo, lblFechas, lblEstado, lblPrecio);

        if ("VENCIDO".equalsIgnoreCase(tipo) && rental.getMulta() > 0) {
            Label lblMulta = new Label(String.format("Multa actual: %.2f EUR", rental.getMulta()));
            lblMulta.setTextFill(Color.web("#ff6b6b"));
            lblMulta.setFont(Font.font("System", FontWeight.BOLD, 14));
            info.getChildren().add(lblMulta);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox botones = new HBox(10);
        botones.setAlignment(Pos.CENTER_RIGHT);

        if ("ACTIVO".equalsIgnoreCase(tipo)) {
            Button btnVer = createActionButton("VER", "#e94560", "white");
            btnVer.setOnAction(e -> playMovie(rental.getIdPelicula(), titulo));

            Button btnDevolver = createActionButton("DEVOLVER", "#4ecdc4", "#0f0f1a");
            btnDevolver.setOnAction(e -> devolverPelicula(rental.getId()));

            botones.getChildren().addAll(btnVer, btnDevolver);

        } else if ("VENCIDO".equalsIgnoreCase(tipo)) {
            Button btnDevolver = createActionButton("DEVOLVER", "#4ecdc4", "#0f0f1a");
            btnDevolver.setOnAction(e -> devolverPelicula(rental.getId()));

            botones.getChildren().add(btnDevolver);
        }

        card.getChildren().addAll(info, spacer, botones);
        return card;
    }

    private String createFechaText(Rental rental, String tipo) {
        String fechaAlquiler = rental.getFechaAlquiler() != null
                ? rental.getFechaAlquiler().toString()
                : "sin fecha";

        String fechaDevolucion = rental.getFechaDevolucion() != null
                ? rental.getFechaDevolucion().toString()
                : "sin fecha";

        if ("ACTIVO".equalsIgnoreCase(tipo)) {
            return "Alquilado: " + fechaAlquiler + "   Devolver antes del: " + fechaDevolucion;
        }

        if ("VENCIDO".equalsIgnoreCase(tipo)) {
            return "Alquilado: " + fechaAlquiler + "   Venció el: " + fechaDevolucion;
        }

        return "Alquilado: " + fechaAlquiler + "   Devuelto / finalizado";
    }

    private Color getEstadoColor(String estado) {
        if ("ACTIVO".equalsIgnoreCase(estado)) {
            return Color.web("#4ecdc4");
        }

        if ("VENCIDO".equalsIgnoreCase(estado)) {
            return Color.web("#ff6b6b");
        }

        if ("DEVUELTO".equalsIgnoreCase(estado)) {
            return Color.web("#a0a0a0");
        }

        return Color.WHITE;
    }

    private Button createActionButton(String text, String background, String textColor) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: " + background + ";" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 22;" +
                        "-fx-background-radius: 5;");
        return btn;
    }

    private Movie loadMovieSafely(int idPelicula) {
        try {
            MovieFunctions movieFunctions = new MovieFunctions();
            return movieFunctions.getMovieById(idPelicula);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void devolverPelicula(int idAlquiler) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText(null);
        confirm.setContentText("¿Quieres devolver esta película?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    RentalFunctions rentalFunctions = new RentalFunctions();
                    rentalFunctions.returnMovie(idAlquiler);

                    showAlert(Alert.AlertType.INFORMATION, "Película devuelta correctamente.");
                    loadRentalsFromDb();

                    if (userPanel != null) {
                        userPanel.refreshRentalSection();
                    }

                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error al devolver la película.");
                    e.printStackTrace();
                }
            }
        });
    }

    private void playMovie(int idPelicula, String tituloFallback) {
        new Thread(() -> {
            try {
                MovieFunctions movieFunctions = new MovieFunctions();
                Movie movie = movieFunctions.getMovieById(idPelicula);

                Platform.runLater(() -> {
                    if (movie == null) {
                        showAlert(Alert.AlertType.WARNING, "No se encontró información para esta película.");
                        return;
                    }

                    String videoPath = movie.getVideo();

                    if (videoPath == null || videoPath.isBlank()
                            || videoPath.equalsIgnoreCase("url_video")
                            || videoPath.startsWith("http")) {
                        showAlert(Alert.AlertType.INFORMATION,
                                "Esta película no tiene un tráiler local disponible.");
                        return;
                    }

                    openVideoPlayer(movie.getTitulo(), normalizarRutaRecurso(videoPath));
                });

            } catch (SQLException e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error al cargar los datos de la película."));
                e.printStackTrace();
            }
        }).start();
    }

    private String normalizarRutaRecurso(String path) {
        if (path == null) {
            return "";
        }

        String normalized = path.replace("\\", "/").trim();

        String prefix = "src/main/resources/";
        if (normalized.startsWith(prefix)) {
            normalized = normalized.substring(prefix.length());
        }

        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        return normalized;
    }

    private void openVideoPlayer(String titulo, String videoPath) {
        try {
            URL resource = getClass().getClassLoader().getResource(videoPath);

            if (resource == null) {
                showAlert(Alert.AlertType.ERROR,
                        "No se encontró el vídeo:\n" + videoPath +
                                "\n\nLa ruta debe ser relativa a src/main/resources.\nEjemplo: trailers/t_el_padrino.mp4");
                return;
            }

            Media media = new Media(resource.toExternalForm());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            MediaView mediaView = new MediaView(mediaPlayer);

            mediaView.setPreserveRatio(true);
            mediaView.setFitWidth(900);
            mediaView.setFitHeight(500);

            Button btnPlay = createActionButton("PLAY", "#e94560", "white");
            Button btnPause = createActionButton("PAUSE", "#4ecdc4", "#0f0f1a");
            Button btnStop = createActionButton("RESET", "#a0a0a0", "#0f0f1a");

            btnPlay.setOnAction(e -> mediaPlayer.play());
            btnPause.setOnAction(e -> mediaPlayer.pause());
            btnStop.setOnAction(e -> mediaPlayer.stop());

            HBox controls = new HBox(10, btnPlay, btnPause, btnStop);
            controls.setPadding(new Insets(12));
            controls.setAlignment(Pos.CENTER);
            controls.setStyle("-fx-background-color: #1a1a2e;");

            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: #0f0f1a;");
            root.setCenter(mediaView);
            root.setBottom(controls);

            Stage stage = new Stage();
            stage.setTitle("Avanti - " + titulo);
            stage.initModality(Modality.NONE);

            Scene scene = new Scene(root, 900, 560);
            stage.setScene(scene);

            stage.setOnCloseRequest(e -> {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            });

            mediaPlayer.setOnError(() -> showAlert(Alert.AlertType.ERROR,
                    "Error del reproductor:\n" + mediaPlayer.getError()));

            media.setOnError(() -> showAlert(Alert.AlertType.ERROR,
                    "Error al cargar el archivo de vídeo:\n" + media.getError()));

            stage.show();
            mediaPlayer.play();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR,
                    "No se pudo reproducir el vídeo:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
