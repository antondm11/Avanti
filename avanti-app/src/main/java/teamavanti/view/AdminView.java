package teamavanti.view;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import teamavanti.bbdd.DatabaseManager;
import teamavanti.util.SessionManager;

public class AdminView extends BorderPane {

    private MainFrame mainFrame;
    private VBox centralPanel;

    public AdminView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        createUI();
        showWelcome();
    }

    private void createUI() {
        setStyle("-fx-background-color: #0f0f1a;");

        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 25, 15, 25));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #e94560; -fx-border-width: 0 0 2 0;");

        Label lblLogo = new Label("AVANTI - ADMIN");
        lblLogo.setFont(Font.font("System", FontWeight.BOLD, 24));
        lblLogo.setTextFill(Color.web("#e94560"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblUsuario = new Label("Admin: " +
                (SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getNombre() : "Admin"));
        lblUsuario.setTextFill(Color.web("#a0a0a0"));

        Button btnLogout = new Button("Cerrar sesión");
        btnLogout.setStyle("-fx-background-color: transparent; -fx-text-fill: #e94560; -fx-border-color: #e94560;");
        btnLogout.setOnAction(e -> mainFrame.logout());

        topBar.getChildren().addAll(lblLogo, spacer, lblUsuario, btnLogout);

        VBox navBar = new VBox(10);
        navBar.setPadding(new Insets(20, 15, 20, 15));
        navBar.setStyle("-fx-background-color: #16213e;");
        navBar.setPrefWidth(200);
        navBar.setAlignment(Pos.TOP_CENTER);

        Label lblMenu = new Label("GESTIÓN");
        lblMenu.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblMenu.setTextFill(Color.web("#a0a0a0"));

        Button btnPeliculas = createNavButton("Películas");
        Button btnAnadir = createNavButton("Añadir Película");
        Button btnAlquileres = createNavButton("Ver Alquileres");
        Button btnIngresos = createNavButton("Ingresos Totales");

        btnPeliculas.setOnAction(e -> showGestionPeliculas());
        btnAnadir.setOnAction(e -> showAnadirPelicula());
        btnAlquileres.setOnAction(e -> showAlquileres());
        btnIngresos.setOnAction(e -> showIngresos());

        navBar.getChildren().addAll(lblMenu, btnPeliculas, btnAnadir, btnAlquileres, btnIngresos);

        centralPanel = new VBox(20);
        centralPanel.setPadding(new Insets(30));
        centralPanel.setAlignment(Pos.TOP_CENTER);
        centralPanel.setStyle("-fx-background-color: #0f0f1a;");

        setTop(topBar);
        setLeft(navBar);
        setCenter(centralPanel);
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);

        String baseStyle = "-fx-background-color: transparent; -fx-text-fill: #a0a0a0; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15;";
        String hoverStyle = "-fx-background-color: #e94560; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15;";

        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));

        return btn;
    }

    private void showWelcome() {
        centralPanel.getChildren().clear();

        Label lbl = createTitle("Panel de Administración");
        Label sub = new Label("Gestiona películas, alquileres e ingresos del videoclub Avanti.");
        sub.setTextFill(Color.web("#a0a0a0"));
        sub.setFont(Font.font(15));

        centralPanel.getChildren().addAll(lbl, sub);
    }

    private void showGestionPeliculas() {
        centralPanel.getChildren().clear();

        Label lbl = createTitle("GESTIÓN DE PELÍCULAS");

        TableView<PeliculaRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(430);

        TableColumn<PeliculaRow, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<PeliculaRow, String> colTitulo = new TableColumn<>("Título");
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));

        TableColumn<PeliculaRow, String> colDirector = new TableColumn<>("Director");
        colDirector.setCellValueFactory(new PropertyValueFactory<>("director"));

        TableColumn<PeliculaRow, Integer> colAno = new TableColumn<>("Año");
        colAno.setCellValueFactory(new PropertyValueFactory<>("ano"));

        TableColumn<PeliculaRow, String> colGenero = new TableColumn<>("Género");
        colGenero.setCellValueFactory(new PropertyValueFactory<>("genero"));

        TableColumn<PeliculaRow, Double> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        TableColumn<PeliculaRow, String> colDisponible = new TableColumn<>("Disponible");
        colDisponible.setCellValueFactory(new PropertyValueFactory<>("disponible"));

        table.getColumns().add(colId);
        table.getColumns().add(colTitulo);
        table.getColumns().add(colDirector);
        table.getColumns().add(colAno);
        table.getColumns().add(colGenero);
        table.getColumns().add(colPrecio);
        table.getColumns().add(colDisponible);
        table.setItems(loadPeliculas());

        Button btnBorrar = new Button("BORRAR PELÍCULA SELECCIONADA");
        btnBorrar.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-font-weight: bold;");
        btnBorrar.setOnAction(e -> {
            PeliculaRow selected = table.getSelectionModel().getSelectedItem();

            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Selecciona una película para borrarla.");
                return;
            }

            borrarPelicula(selected.getId());
            table.setItems(loadPeliculas());
        });

        centralPanel.getChildren().addAll(lbl, table, btnBorrar);
    }

    private void showAnadirPelicula() {
        centralPanel.getChildren().clear();

        Label lbl = createTitle("AÑADIR NUEVA PELÍCULA");

        GridPane form = new GridPane();
        form.setVgap(12);
        form.setHgap(12);
        form.setAlignment(Pos.CENTER);

        String estilo = "-fx-background-color: #16213e; -fx-text-fill: white; -fx-prompt-text-fill: #777;";

        TextField txtTitulo = createTextField("Título", estilo);
        TextField txtDirector = createTextField("Director", estilo);
        TextField txtAnio = createTextField("Año", estilo);
        TextField txtDuracion = createTextField("Duración en minutos", estilo);
        TextField txtPrecio = createTextField("Precio", estilo);
        TextField txtGeneroId = createTextField("ID género", estilo);

        TextArea txtSinopsis = new TextArea();
        txtSinopsis.setPromptText("Sinopsis");
        txtSinopsis.setStyle(estilo);
        txtSinopsis.setPrefRowCount(3);

        TextField txtImagen = createTextField("Ruta de imagen de portada", estilo);
        TextField txtVideo = createTextField("Ruta del vídeo o escena", estilo);

        Button btnImagen = new Button("Seleccionar portada");
        btnImagen.setOnAction(e -> seleccionarArchivo(txtImagen, "Seleccionar imagen de portada"));

        Button btnVideo = new Button("Seleccionar vídeo");
        btnVideo.setOnAction(e -> seleccionarArchivo(txtVideo, "Seleccionar vídeo"));

        form.addRow(0, createLabel("Título:"), txtTitulo);
        form.addRow(1, createLabel("Director:"), txtDirector);
        form.addRow(2, createLabel("Año:"), txtAnio);
        form.addRow(3, createLabel("Duración:"), txtDuracion);
        form.addRow(4, createLabel("Precio:"), txtPrecio);
        form.addRow(5, createLabel("ID género:"), txtGeneroId);
        form.addRow(6, createLabel("Sinopsis:"), txtSinopsis);
        form.addRow(7, createLabel("Portada:"), new HBox(10, txtImagen, btnImagen));
        form.addRow(8, createLabel("Vídeo:"), new HBox(10, txtVideo, btnVideo));

        Button btnGuardar = new Button("GUARDAR PELÍCULA");
        btnGuardar.setStyle("-fx-background-color: #4ecdc4; -fx-text-fill: #0f0f1a; -fx-font-weight: bold;");
        btnGuardar.setOnAction(e -> {
            try {
                guardarPelicula(
                        txtTitulo.getText(),
                        txtDirector.getText(),
                        txtAnio.getText(),
                        txtSinopsis.getText(),
                        txtDuracion.getText(),
                        txtPrecio.getText(),
                        txtImagen.getText(),
                        txtVideo.getText(),
                        txtGeneroId.getText());

                showAlert(Alert.AlertType.INFORMATION, "Película añadida correctamente.");

                txtTitulo.clear();
                txtDirector.clear();
                txtAnio.clear();
                txtDuracion.clear();
                txtPrecio.clear();
                txtGeneroId.clear();
                txtSinopsis.clear();
                txtImagen.clear();
                txtVideo.clear();

            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Revisa los campos numéricos: año, duración, precio e ID de género.");
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error al guardar la película en la base de datos.");
                ex.printStackTrace();
            } catch (IllegalArgumentException ex) {
                showAlert(Alert.AlertType.WARNING, ex.getMessage());
            }
        });

        centralPanel.getChildren().addAll(lbl, form, btnGuardar);
    }

    private void showAlquileres() {
        centralPanel.getChildren().clear();

        Label lbl = createTitle("ALQUILERES");

        TableView<AlquilerRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(480);

        TableColumn<AlquilerRow, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<AlquilerRow, String> colUsuario = new TableColumn<>("Usuario");
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));

        TableColumn<AlquilerRow, String> colPelicula = new TableColumn<>("Película");
        colPelicula.setCellValueFactory(new PropertyValueFactory<>("pelicula"));

        TableColumn<AlquilerRow, String> colFechaAlquiler = new TableColumn<>("Fecha alquiler");
        colFechaAlquiler.setCellValueFactory(new PropertyValueFactory<>("fechaAlquiler"));

        TableColumn<AlquilerRow, String> colFechaDevolucion = new TableColumn<>("Fecha devolución");
        colFechaDevolucion.setCellValueFactory(new PropertyValueFactory<>("fechaDevolucion"));

        TableColumn<AlquilerRow, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        TableColumn<AlquilerRow, Double> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioPagado"));

        TableColumn<AlquilerRow, Double> colMulta = new TableColumn<>("Multa");
        colMulta.setCellValueFactory(new PropertyValueFactory<>("multaActual"));

        table.getColumns().add(colId);
        table.getColumns().add(colUsuario);
        table.getColumns().add(colPelicula);
        table.getColumns().add(colFechaAlquiler);
        table.getColumns().add(colFechaDevolucion);
        table.getColumns().add(colEstado);
        table.getColumns().add(colPrecio);
        table.getColumns().add(colMulta);

        table.setItems(loadAlquileres());

        centralPanel.getChildren().addAll(lbl, table);
    }

    private void showIngresos() {
        centralPanel.getChildren().clear();

        Label lbl = createTitle("INGRESOS TOTALES");

        double ingresos = calcularSuma("precio_pagado");
        double multas = calcularSuma("multa");
        double total = ingresos + multas;

        VBox resumen = new VBox(10);
        resumen.setAlignment(Pos.CENTER);
        resumen.setPadding(new Insets(20));
        resumen.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 10;");
        resumen.setMaxWidth(400);

        resumen.getChildren().addAll(
                createResumenLabel("Ingresos por alquileres:", ingresos),
                createResumenLabel("Multas registradas:", multas),
                createResumenLabel("TOTAL:", total));

        centralPanel.getChildren().addAll(lbl, resumen);
    }

    private ObservableList<PeliculaRow> loadPeliculas() {
        ObservableList<PeliculaRow> peliculas = FXCollections.observableArrayList();

        String sql = """
                SELECT p.id, p.titulo, p.director, p.ano, g.nombre AS genero, p.precio, p.disponible
                FROM pelicula p
                JOIN genero g ON p.id_genero = g.id
                ORDER BY p.titulo
                """;

        Connection conn = DatabaseManager.getInstance().connectToDb();

        if (conn == null) {
            return peliculas;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                peliculas.add(new PeliculaRow(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("director"),
                        rs.getInt("ano"),
                        rs.getString("genero"),
                        rs.getDouble("precio"),
                        rs.getBoolean("disponible") ? "Sí" : "No"));
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "No se pudieron cargar las películas.");
            e.printStackTrace();
        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }

        return peliculas;
    }

    private ObservableList<AlquilerRow> loadAlquileres() {
        ObservableList<AlquilerRow> alquileres = FXCollections.observableArrayList();

        String sql = """
                SELECT id, usuario, pelicula, fecha_alquiler, fecha_devolucion,
                       estado, precio_pagado, multa_actual
                FROM v_alquileres
                ORDER BY fecha_alquiler DESC
                """;

        Connection conn = DatabaseManager.getInstance().connectToDb();

        if (conn == null) {
            return alquileres;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                alquileres.add(new AlquilerRow(
                        rs.getInt("id"),
                        rs.getString("usuario"),
                        rs.getString("pelicula"),
                        String.valueOf(rs.getDate("fecha_alquiler")),
                        String.valueOf(rs.getDate("fecha_devolucion")),
                        rs.getString("estado"),
                        rs.getDouble("precio_pagado"),
                        rs.getDouble("multa_actual")));
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "No se pudieron cargar los alquileres.");
            e.printStackTrace();
        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }

        return alquileres;
    }

    private void guardarPelicula(
            String titulo,
            String director,
            String ano,
            String sinopsis,
            String duracion,
            String precio,
            String imagen,
            String video,
            String idGenero) throws SQLException {

        if (titulo.isBlank() || director.isBlank() || ano.isBlank() || duracion.isBlank()
                || precio.isBlank() || idGenero.isBlank()) {
            throw new IllegalArgumentException(
                    "Rellena los campos obligatorios: título, director, año, duración, precio e ID género.");
        }

        String sql = """
                INSERT INTO pelicula
                (titulo, director, ano, sinopsis, duracion, precio, imagen, video, disponible, id_genero)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, TRUE, ?)
                """;

        Connection conn = DatabaseManager.getInstance().connectToDb();

        if (conn == null) {
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, titulo.trim());
            ps.setString(2, director.trim());
            ps.setInt(3, Integer.parseInt(ano.trim()));
            ps.setString(4, sinopsis.trim());
            ps.setInt(5, Integer.parseInt(duracion.trim()));
            ps.setDouble(6, Double.parseDouble(precio.trim().replace(",", ".")));
            ps.setString(7, imagen.trim());
            ps.setString(8, video.trim());
            ps.setInt(9, Integer.parseInt(idGenero.trim()));

            ps.executeUpdate();

        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }
    }

    private void borrarPelicula(int idPelicula) {
        String sql = "DELETE FROM pelicula WHERE id = ?";

        Connection conn = DatabaseManager.getInstance().connectToDb();

        if (conn == null) {
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPelicula);
            ps.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Película eliminada correctamente.");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "No se pudo borrar la película. Puede tener alquileres asociados.");
            e.printStackTrace();
        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }
    }

    private double calcularSuma(String columna) {
        String sql = "SELECT COALESCE(SUM(" + columna + "), 0) AS total FROM alquiler";

        Connection conn = DatabaseManager.getInstance().connectToDb();

        if (conn == null) {
            return 0.0;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("total");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "No se pudieron calcular los ingresos.");
            e.printStackTrace();
        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }

        return 0.0;
    }

    private void seleccionarArchivo(TextField destino, String tituloVentana) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(tituloVentana);

        File archivo = fileChooser.showOpenDialog(getScene().getWindow());

        if (archivo != null) {
            destino.setText(archivo.getPath());
        }
    }

    private TextField createTextField(String prompt, String style) {
        TextField txt = new TextField();
        txt.setPromptText(prompt);
        txt.setStyle(style);
        txt.setPrefWidth(260);
        return txt;
    }

    private Label createLabel(String text) {
        Label lbl = new Label(text);
        lbl.setTextFill(Color.WHITE);
        return lbl;
    }

    private Label createTitle(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 22));
        lbl.setTextFill(Color.web("#e94560"));
        return lbl;
    }

    private Label createResumenLabel(String text, double valor) {
        Label lbl = new Label(text + " " + String.format("%.2f EUR", valor));
        lbl.setTextFill(Color.WHITE);
        lbl.setFont(Font.font(16));
        return lbl;
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class PeliculaRow {
        private int id;
        private String titulo;
        private String director;
        private int ano;
        private String genero;
        private double precio;
        private String disponible;

        public PeliculaRow(int id, String titulo, String director, int ano, String genero, double precio,
                String disponible) {
            this.id = id;
            this.titulo = titulo;
            this.director = director;
            this.ano = ano;
            this.genero = genero;
            this.precio = precio;
            this.disponible = disponible;
        }

        public int getId() {
            return id;
        }

        public String getTitulo() {
            return titulo;
        }

        public String getDirector() {
            return director;
        }

        public int getAno() {
            return ano;
        }

        public String getGenero() {
            return genero;
        }

        public double getPrecio() {
            return precio;
        }

        public String getDisponible() {
            return disponible;
        }
    }

    public static class AlquilerRow {
        private int id;
        private String usuario;
        private String pelicula;
        private String fechaAlquiler;
        private String fechaDevolucion;
        private String estado;
        private double precioPagado;
        private double multaActual;

        public AlquilerRow(int id, String usuario, String pelicula, String fechaAlquiler,
                String fechaDevolucion, String estado, double precioPagado, double multaActual) {
            this.id = id;
            this.usuario = usuario;
            this.pelicula = pelicula;
            this.fechaAlquiler = fechaAlquiler;
            this.fechaDevolucion = fechaDevolucion;
            this.estado = estado;
            this.precioPagado = precioPagado;
            this.multaActual = multaActual;
        }

        public int getId() {
            return id;
        }

        public String getUsuario() {
            return usuario;
        }

        public String getPelicula() {
            return pelicula;
        }

        public String getFechaAlquiler() {
            return fechaAlquiler;
        }

        public String getFechaDevolucion() {
            return fechaDevolucion;
        }

        public String getEstado() {
            return estado;
        }

        public double getPrecioPagado() {
            return precioPagado;
        }

        public double getMultaActual() {
            return multaActual;
        }
    }
}