package teamavanti.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import teamavanti.bbdd.MovieFunctions;
import teamavanti.model.Movie;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CatalogoSection extends VBox {

    private FlowPane gridPeliculas;
    private TextField txtBuscar;
    private List<Movie> todasLasPeliculas = new ArrayList<>();

    public CatalogoSection() {
        createUI();
        loadMoviesFromDb();
    }

    private void createUI() {
        setSpacing(15);
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(10));
        // El VBox debe crecer para llenar el espacio disponible
        VBox.setVgrow(this, Priority.ALWAYS);

        Label lblTitulo = new Label("CATÁLOGO DE PELÍCULAS");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 24));
        lblTitulo.setTextFill(Color.web("#e94560"));

        HBox busquedaBox = new HBox(10);
        busquedaBox.setAlignment(Pos.CENTER);

        txtBuscar = new TextField();
        txtBuscar.setPromptText("Buscar por título, director o año...");
        txtBuscar.setPrefWidth(400);
        txtBuscar.setStyle("-fx-background-color: #16213e; -fx-text-fill: white; -fx-prompt-text-fill: #666;");
        txtBuscar.textProperty().addListener((obs, old, val) -> filterMovies());

        Button btnBuscar = new Button("Buscar");
        btnBuscar.setStyle("-fx-background-color: #e94560; -fx-text-fill: white;");
        btnBuscar.setOnAction(e -> filterMovies());

        busquedaBox.getChildren().addAll(txtBuscar, btnBuscar);

        gridPeliculas = new FlowPane();
        gridPeliculas.setHgap(20);
        gridPeliculas.setVgap(20);
        gridPeliculas.setAlignment(Pos.CENTER);
        gridPeliculas.setPadding(new Insets(10));

        ScrollPane scroll = new ScrollPane(gridPeliculas);
        scroll.setStyle("-fx-background: #0f0f1a; -fx-background-color: transparent;");
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        // Crucial: que el scroll crezca verticalmente
        VBox.setVgrow(scroll, Priority.ALWAYS);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        getChildren().addAll(lblTitulo, busquedaBox, scroll);
    }

    private void loadMoviesFromDb() {
        // Carga en hilo de fondo para no bloquear la UI
        new Thread(() -> {
            try {
                MovieFunctions mf = new MovieFunctions();
                List<Movie> lista = mf.getMovies();
                Platform.runLater(() -> {
                    todasLasPeliculas = lista;
                    showMovies(todasLasPeliculas);
                });
            } catch (SQLException e) {
                Platform.runLater(() -> showError("Error al conectar con la base de datos."));
                e.printStackTrace();
            }
        }).start();
    }

    private void filterMovies() {
        String query = txtBuscar.getText().toLowerCase().trim();
        List<Movie> filtered = todasLasPeliculas.stream()
                .filter(m -> m.getTitulo().toLowerCase().contains(query)
                        || m.getDirector().toLowerCase().contains(query)
                        || String.valueOf(m.getAno()).contains(query))
                .collect(Collectors.toList());
        showMovies(filtered);
    }

    private void showMovies(List<Movie> movies) {
        gridPeliculas.getChildren().clear();
        if (movies.isEmpty()) {
            Label vacio = new Label("No se encontraron películas.");
            vacio.setTextFill(Color.web("#a0a0a0"));
            gridPeliculas.getChildren().add(vacio);
        } else {
            for (Movie m : movies) {
                gridPeliculas.getChildren().add(createMovieCard(m));
            }
        }
    }

    private void showError(String msg) {
        gridPeliculas.getChildren().clear();
        Label err = new Label(msg);
        err.setTextFill(Color.web("#ff6b6b"));
        gridPeliculas.getChildren().add(err);
    }

    private VBox createMovieCard(Movie m) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setPrefWidth(200);
        card.setStyle(
                "-fx-background-color: #1a1a2e; -fx-background-radius: 10; -fx-border-color: #16213e; -fx-border-radius: 10;");

        StackPane imgPlaceholder = new StackPane();
        imgPlaceholder.setPrefSize(170, 250);
        imgPlaceholder.setStyle("-fx-background-color: #16213e; -fx-background-radius: 5;");
        Label lblImg = new Label("🎬");
        lblImg.setFont(Font.font(40));
        imgPlaceholder.getChildren().add(lblImg);

        Label lblTitulo = new Label(m.getTitulo());
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblTitulo.setTextFill(Color.WHITE);
        lblTitulo.setWrapText(true);

        Label lblInfo = new Label(m.getAno() + " | " + m.getDirector());
        lblInfo.setTextFill(Color.web("#a0a0a0"));
        lblInfo.setFont(Font.font(11));
        lblInfo.setWrapText(true);

        Label lblGenero = new Label(m.getGenero() != null && !m.getGenero().isEmpty() ? m.getGenero() : "");
        lblGenero.setTextFill(Color.web("#4ecdc4"));
        lblGenero.setFont(Font.font(11));

        Label lblPrecio = new Label(String.format("%.2f EUR", m.getPrecio()));
        lblPrecio.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblPrecio.setTextFill(Color.web("#e94560"));

        Label lblDisp = new Label(m.isDisponible() ? "✔ Disponible" : "✘ No disponible");
        lblDisp.setTextFill(m.isDisponible() ? Color.web("#4ecdc4") : Color.web("#ff6b6b"));
        lblDisp.setFont(Font.font(11));

        card.getChildren().addAll(imgPlaceholder, lblTitulo, lblInfo, lblGenero, lblPrecio, lblDisp);
        return card;
    }
}
