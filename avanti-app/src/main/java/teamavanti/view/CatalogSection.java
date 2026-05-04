package teamavanti.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import teamavanti.bbdd.MovieFunctions;
import teamavanti.model.Movie;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CatalogSection extends VBox {

    private FlowPane gridMovies;
    private TextField txtSearch;
    private List<Movie> allMovies = new ArrayList<>();

    public CatalogSection() {
        createUI();
        loadMoviesFromDb();
    }

    private void createUI() {
        setSpacing(15);
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(10));
        // El VBox debe crecer para llenar el espacio disponible
        VBox.setVgrow(this, Priority.ALWAYS);

        Label lblTitle = new Label("CATÁLOGO DE PELÍCULAS");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        lblTitle.setTextFill(Color.web("#e94560"));

        HBox researchBox = new HBox(10);
        researchBox.setAlignment(Pos.CENTER);

        txtSearch = new TextField();
        txtSearch.setPromptText("Buscar por título, director o año...");
        txtSearch.setPrefWidth(400);
        txtSearch.setStyle("-fx-background-color: #16213e; -fx-text-fill: white; -fx-prompt-text-fill: #666;");
        txtSearch.textProperty().addListener((obs, old, val) -> filterMovies());

        Button btnSearch = new Button("Buscar");
        btnSearch.setStyle("-fx-background-color: #e94560; -fx-text-fill: white;");
        btnSearch.setOnAction(e -> filterMovies());

        researchBox.getChildren().addAll(txtSearch, btnSearch);

        gridMovies = new FlowPane();
        gridMovies.setHgap(20);
        gridMovies.setVgap(20);
        gridMovies.setAlignment(Pos.CENTER);
        gridMovies.setPadding(new Insets(10));

        ScrollPane scroll = new ScrollPane(gridMovies);
        scroll.setStyle("-fx-background: #0f0f1a; -fx-background-color: transparent;");
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        // Crucial: que el scroll crezca verticalmente
        VBox.setVgrow(scroll, Priority.ALWAYS);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        getChildren().addAll(lblTitle, researchBox, scroll);
    }

    private void loadMoviesFromDb() {
        // Carga en hilo de fondo para no bloquear la UI
        new Thread(() -> {
            try {
                MovieFunctions mf = new MovieFunctions();
                List<Movie> list = mf.getMovies();
                Platform.runLater(() -> {
                    allMovies = list;
                    showMovies(allMovies);
                });
            } catch (SQLException e) {
                Platform.runLater(() -> showError("Error al conectar con la base de datos."));
                e.printStackTrace();
            }
        }).start();
    }

    private void filterMovies() {
        String query = txtSearch.getText().toLowerCase().trim();
        List<Movie> filtered = allMovies.stream()
                .filter(m -> m.getTitulo().toLowerCase().contains(query)
                        || m.getDirector().toLowerCase().contains(query)
                        || String.valueOf(m.getAno()).contains(query))
                .collect(Collectors.toList());
        showMovies(filtered);
    }

    private void showMovies(List<Movie> movies) {
        gridMovies.getChildren().clear();
        if (movies.isEmpty()) {
            Label empty = new Label("No se encontraron películas.");
            empty.setTextFill(Color.web("#a0a0a0"));
            gridMovies.getChildren().add(empty);
        } else {
            for (Movie m : movies) {
                gridMovies.getChildren().add(createMovieCard(m));
            }
        }
    }

    private void showError(String msg) {
        gridMovies.getChildren().clear();
        Label err = new Label(msg);
        err.setTextFill(Color.web("#ff6b6b"));
        gridMovies.getChildren().add(err);
    }

    private VBox createMovieCard(Movie m) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setPrefWidth(200);
        card.setStyle(
                "-fx-background-color: #1a1a2e; -fx-background-radius: 10; -fx-border-color: #16213e; -fx-border-radius: 10;");

        StackPane imgPlaceholder = createPosterBox(m);

        Label lblMovieTitle = new Label(m.getTitulo());
        lblMovieTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblMovieTitle.setTextFill(Color.WHITE);
        lblMovieTitle.setWrapText(true);

        Label lblMovieInfo = new Label(m.getAno() + " | " + m.getDirector());
        lblMovieInfo.setTextFill(Color.web("#a0a0a0"));
        lblMovieInfo.setFont(Font.font(11));
        lblMovieInfo.setWrapText(true);

        Label lblGenre = new Label(m.getGenero() != null && !m.getGenero().isEmpty() ? m.getGenero() : "");
        lblGenre.setTextFill(Color.web("#4ecdc4"));
        lblGenre.setFont(Font.font(11));

        Label lblPrice = new Label(String.format("%.2f EUR", m.getPrecio()));
        lblPrice.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblPrice.setTextFill(Color.web("#e94560"));

        Label lblAvailable = new Label(m.isDisponible() ? "✔ Disponible" : "✘ No disponible");
        lblAvailable.setTextFill(m.isDisponible() ? Color.web("#4ecdc4") : Color.web("#ff6b6b"));
        lblAvailable.setFont(Font.font(11));

        card.getChildren().addAll(imgPlaceholder, lblMovieTitle, lblMovieInfo, lblGenre, lblPrice, lblAvailable);
        return card;
    }

    private StackPane createPosterBox(Movie movie) {
        StackPane posterBox = new StackPane();
        posterBox.setPrefSize(170, 250);
        posterBox.setMinSize(170, 250);
        posterBox.setMaxSize(170, 250);
        posterBox.setStyle("-fx-background-color: #16213e; -fx-background-radius: 5;");

        ImageView poster = createPosterImageView(movie.getImagen());

        if (poster != null) {
            posterBox.getChildren().add(poster);
        } else {
            Label fallback = new Label("🎬");
            fallback.setFont(Font.font(40));
            fallback.setTextFill(Color.web("#a0a0a0"));
            posterBox.getChildren().add(fallback);
        }

        return posterBox;
    }

    private ImageView createPosterImageView(String imagePath) {
        try {
            String normalizedPath = normalizarRutaRecurso(imagePath);

            if (normalizedPath == null || normalizedPath.isBlank()
                    || normalizedPath.equalsIgnoreCase("url_imagen")
                    || normalizedPath.startsWith("http")) {
                return null;
            }

            URL resource = getClass().getClassLoader().getResource(normalizedPath);

            if (resource == null) {
                System.err.println("No se encontró la imagen: " + normalizedPath);
                return null;
            }

            Image image = new Image(resource.toExternalForm(), 170, 250, false, true);

            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(170);
            imageView.setFitHeight(250);
            imageView.setPreserveRatio(false);
            imageView.setSmooth(true);

            return imageView;

        } catch (Exception e) {
            System.err.println("Error al cargar poster: " + imagePath);
            e.printStackTrace();
            return null;
        }
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
}
