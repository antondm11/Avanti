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
import teamavanti.bbdd.UserFunctions;
import teamavanti.model.User;

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

        Label lblUser = new Label("Admin: " +
                (SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getNombre() : "Admin"));
        lblUser.setTextFill(Color.web("#a0a0a0"));

        Button btnLogout = new Button("Cerrar sesión");
        btnLogout.setStyle("-fx-background-color: transparent; -fx-text-fill: #e94560; -fx-border-color: #e94560;");
        btnLogout.setOnAction(e -> mainFrame.logout());

        topBar.getChildren().addAll(lblLogo, spacer, lblUser, btnLogout);

        VBox navBar = new VBox(10);
        navBar.setPadding(new Insets(20, 15, 20, 15));
        navBar.setStyle("-fx-background-color: #16213e;");
        navBar.setPrefWidth(200);
        navBar.setAlignment(Pos.TOP_CENTER);

        Label lblMenu = new Label("GESTIÓN");
        lblMenu.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblMenu.setTextFill(Color.web("#a0a0a0"));

        Button btnMovies = createNavButton("Películas");
        Button btnAdd = createNavButton("Añadir Película");
        Button btnRental = createNavButton("Ver Alquileres");
        Button btnIncomes = createNavButton("Ingresos Totales");
        Button btnManageUser = createNavButton("Gestionar Usuarios");

        btnMovies.setOnAction(e -> showMovieManage());
        btnAdd.setOnAction(e -> showAddMovie());
        btnRental.setOnAction(e -> showRentals());
        btnIncomes.setOnAction(e -> showIncomes());
        btnManageUser.setOnAction(e -> showManageUser());

        navBar.getChildren().addAll(lblMenu, btnMovies, btnAdd, btnRental, btnIncomes, btnManageUser);

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

    private void showManageUser() {
        centralPanel.getChildren().clear();

        Label lbl = createTitle("GESTIÓN DE USUARIOS");

        TableView<UserRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(300);

        TableColumn<UserRow, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<UserRow, String> colName = new TableColumn<>("Nombre");
        colName.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<UserRow, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<UserRow, String> colRole = new TableColumn<>("Rol");
        colRole.setCellValueFactory(new PropertyValueFactory<>("rol"));

        table.getColumns().add(colId);
        table.getColumns().add(colName);
        table.getColumns().add(colEmail);
        table.getColumns().add(colRole);
        table.setItems(loadUsers());

        // Buttons
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER);

        Button btnAddUser = new Button("AÑADIR USUARIO");
        btnAddUser.setStyle("-fx-background-color: #4ecdc4; -fx-text-fill: #0f0f1a; -fx-font-weight: bold;");
        btnAddUser.setOnAction(e -> showAddUser());

        Button btnDeleteUser = new Button("ELIMINAR SELECCIONADO");
        btnDeleteUser.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-font-weight: bold;");
        btnDeleteUser.setOnAction(e -> {
            UserRow selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Selecciona un usuario para eliminar.");
                return;
            }
            try {
                new UserFunctions().deleteUser(selected.getId());
                table.setItems(loadUsers());
                showAlert(Alert.AlertType.INFORMATION, "Usuario eliminado correctamente.");
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "No se pudo eliminar el usuario.");
                ex.printStackTrace();
            }
        });

        TextField txtNewRole = createTextField("Nuevo rol (admin/cliente)",
                "-fx-background-color: #16213e; -fx-text-fill: white;");
        txtNewRole.setPrefWidth(180);

        Button btnChangeRole = new Button("CAMBIAR ROL");
        btnChangeRole.setStyle("-fx-background-color: #fca311; -fx-text-fill: #0f0f1a; -fx-font-weight: bold;");
        btnChangeRole.setOnAction(e -> {
            UserRow selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Selecciona un usuario para cambiar su rol.");
                return;
            }
            String role = txtNewRole.getText().trim().toLowerCase();
            if (role.isEmpty() || (!role.equals("admin") && !role.equals("cliente"))) {
                showAlert(Alert.AlertType.WARNING, "Introduce un rol válido: 'admin' o 'cliente'.");
                return;
            }
            try {
                new UserFunctions().updateUserRole(selected.getId(), role);
                table.setItems(loadUsers());
                txtNewRole.clear();
                showAlert(Alert.AlertType.INFORMATION, "Rol actualizado correctamente.");
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "No se pudo cambiar el rol.");
                ex.printStackTrace();
            }
        });

        actionButtons.getChildren().addAll(btnAddUser, btnDeleteUser, txtNewRole, btnChangeRole);

        centralPanel.getChildren().addAll(lbl, table, actionButtons);
    }

    private void showAddUser() {
        centralPanel.getChildren().clear();

        Label lbl = createTitle("AÑADIR NUEVO USUARIO");

        GridPane form = new GridPane();
        form.setVgap(12);
        form.setHgap(12);
        form.setAlignment(Pos.CENTER);

        String style = "-fx-background-color: #16213e; -fx-text-fill: white; -fx-prompt-text-fill: #777;";

        TextField txtName = createTextField("Nombre completo", style);
        TextField txtEmail = createTextField("Correo electrónico", style);
        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Contraseña");
        txtPassword.setStyle(style);
        txtPassword.setPrefWidth(260);
        TextField txtRole = createTextField("Rol (admin/cliente)", style);

        form.addRow(0, createLabel("Nombre:"), txtName);
        form.addRow(1, createLabel("Email:"), txtEmail);
        form.addRow(2, createLabel("Contraseña:"), txtPassword);
        form.addRow(3, createLabel("Rol:"), txtRole);

        Button btnSave = new Button("GUARDAR USUARIO");
        btnSave.setStyle("-fx-background-color: #4ecdc4; -fx-text-fill: #0f0f1a; -fx-font-weight: bold;");
        btnSave.setOnAction(e -> {
            String name = txtName.getText().trim();
            String email = txtEmail.getText().trim();
            String password = txtPassword.getText();
            String role = txtRole.getText().trim().toLowerCase();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Todos los campos son obligatorios.");
                return;
            }
            if (!role.equals("admin") && !role.equals("cliente")) {
                showAlert(Alert.AlertType.WARNING, "El rol debe ser 'admin' o 'cliente'.");
                return;
            }

            try {
                User u = new User();
                u.setNombre(name);
                u.setEmail(email);
                u.setContrasena(password);
                u.setRol(role);
                new UserFunctions().registerUser(u);

                showAlert(Alert.AlertType.INFORMATION, "Usuario añadido correctamente.");
                showManageUser(); // Volver a la lista
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error al guardar el usuario.");
                ex.printStackTrace();
            }
        });

        Button btnCancel = new Button("CANCELAR");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #a0a0a0;");
        btnCancel.setOnAction(e -> showManageUser());

        HBox buttons = new HBox(15, btnSave, btnCancel);
        buttons.setAlignment(Pos.CENTER);

        centralPanel.getChildren().addAll(lbl, form, buttons);
    }

    private void showMovieManage() {
        centralPanel.getChildren().clear();

        Label lbl = createTitle("GESTIÓN DE PELÍCULAS");

        TableView<MovieRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(430);

        TableColumn<MovieRow, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<MovieRow, String> colTitle = new TableColumn<>("Título");
        colTitle.setCellValueFactory(new PropertyValueFactory<>("titulo"));

        TableColumn<MovieRow, String> colDirector = new TableColumn<>("Director");
        colDirector.setCellValueFactory(new PropertyValueFactory<>("director"));

        TableColumn<MovieRow, Integer> colYear = new TableColumn<>("Año");
        colYear.setCellValueFactory(new PropertyValueFactory<>("ano"));

        TableColumn<MovieRow, String> colGenre = new TableColumn<>("Género");
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genero"));

        TableColumn<MovieRow, Double> colPrice = new TableColumn<>("Precio");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("precio"));

        TableColumn<MovieRow, String> colAvailable = new TableColumn<>("Disponible");
        colAvailable.setCellValueFactory(new PropertyValueFactory<>("disponible"));

        table.getColumns().add(colId);
        table.getColumns().add(colTitle);
        table.getColumns().add(colDirector);
        table.getColumns().add(colYear);
        table.getColumns().add(colGenre);
        table.getColumns().add(colPrice);
        table.getColumns().add(colAvailable);
        table.setItems(loadMovies());

        Button btnDelete = new Button("BORRAR PELÍCULA SELECCIONADA");
        btnDelete.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-font-weight: bold;");
        btnDelete.setOnAction(e -> {
            MovieRow selected = table.getSelectionModel().getSelectedItem();

            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Selecciona una película para borrarla.");
                return;
            }

            deleteMovie(selected.getId());
            table.setItems(loadMovies());
        });

        centralPanel.getChildren().addAll(lbl, table, btnDelete);
    }

    private void showAddMovie() {
        centralPanel.getChildren().clear();

        Label lbl = createTitle("AÑADIR NUEVA PELÍCULA");

        GridPane form = new GridPane();
        form.setVgap(12);
        form.setHgap(12);
        form.setAlignment(Pos.CENTER);

        String style = "-fx-background-color: #16213e; -fx-text-fill: white; -fx-prompt-text-fill: #777;";

        TextField txtTitle = createTextField("Título", style);
        TextField txtDirector = createTextField("Director", style);
        TextField txtYear = createTextField("Año", style);
        TextField txtDuration = createTextField("Duración en minutos", style);
        TextField txtPrice = createTextField("Precio", style);
        TextField txtGenreId = createTextField("ID género", style);

        TextArea txtSynopsis = new TextArea();
        txtSynopsis.setPromptText("Sinopsis");
        txtSynopsis.setStyle(style);
        txtSynopsis.setPrefRowCount(3);

        TextField txtPoster = createTextField("Ruta de imagen de portada", style);
        TextField txtVideo = createTextField("Ruta del vídeo o escena", style);

        Button btnPoster = new Button("Seleccionar portada");
        btnPoster.setOnAction(e -> selectFile(txtPoster, "Seleccionar imagen de portada"));

        Button btnVideo = new Button("Seleccionar vídeo");
        btnVideo.setOnAction(e -> selectFile(txtVideo, "Seleccionar vídeo"));

        form.addRow(0, createLabel("Título:"), txtTitle);
        form.addRow(1, createLabel("Director:"), txtDirector);
        form.addRow(2, createLabel("Año:"), txtYear);
        form.addRow(3, createLabel("Duración:"), txtDuration);
        form.addRow(4, createLabel("Precio:"), txtPrice);
        form.addRow(5, createLabel("ID género:"), txtGenreId);
        form.addRow(6, createLabel("Sinopsis:"), txtSynopsis);
        form.addRow(7, createLabel("Portada:"), new HBox(10, txtPoster, btnPoster));
        form.addRow(8, createLabel("Vídeo:"), new HBox(10, txtVideo, btnVideo));

        Button btnSave = new Button("GUARDAR PELÍCULA");
        btnSave.setStyle("-fx-background-color: #4ecdc4; -fx-text-fill: #0f0f1a; -fx-font-weight: bold;");
        btnSave.setOnAction(e -> {
            try {
                guardarPelicula(
                        txtTitle.getText(),
                        txtDirector.getText(),
                        txtYear.getText(),
                        txtSynopsis.getText(),
                        txtDuration.getText(),
                        txtPrice.getText(),
                        txtPoster.getText(),
                        txtVideo.getText(),
                        txtGenreId.getText());

                showAlert(Alert.AlertType.INFORMATION, "Película añadida correctamente.");

                txtTitle.clear();
                txtDirector.clear();
                txtYear.clear();
                txtDuration.clear();
                txtPrice.clear();
                txtGenreId.clear();
                txtSynopsis.clear();
                txtPoster.clear();
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

        centralPanel.getChildren().addAll(lbl, form, btnSave);
    }

    private void showRentals() {
        centralPanel.getChildren().clear();

        Label lbl = createTitle("ALQUILERES");

        TableView<RentalRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(480);

        TableColumn<RentalRow, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<RentalRow, String> colUser = new TableColumn<>("Usuario");
        colUser.setCellValueFactory(new PropertyValueFactory<>("usuario"));

        TableColumn<RentalRow, String> colMovie = new TableColumn<>("Película");
        colMovie.setCellValueFactory(new PropertyValueFactory<>("pelicula"));

        TableColumn<RentalRow, String> colRentalDate = new TableColumn<>("Fecha alquiler");
        colRentalDate.setCellValueFactory(new PropertyValueFactory<>("fechaAlquiler"));

        TableColumn<RentalRow, String> colReturnDate = new TableColumn<>("Fecha devolución");
        colReturnDate.setCellValueFactory(new PropertyValueFactory<>("fechaDevolucion"));

        TableColumn<RentalRow, String> colStatus = new TableColumn<>("Estado");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("estado"));

        TableColumn<RentalRow, Double> colPrice = new TableColumn<>("Precio");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("precioPagado"));

        TableColumn<RentalRow, Double> colFine = new TableColumn<>("Multa");
        colFine.setCellValueFactory(new PropertyValueFactory<>("multaActual"));

        table.getColumns().add(colId);
        table.getColumns().add(colUser);
        table.getColumns().add(colMovie);
        table.getColumns().add(colRentalDate);
        table.getColumns().add(colReturnDate);
        table.getColumns().add(colStatus);
        table.getColumns().add(colPrice);
        table.getColumns().add(colFine);

        table.setItems(loadRentals());

        centralPanel.getChildren().addAll(lbl, table);
    }

    private void showIncomes() {
        centralPanel.getChildren().clear();

        Label lbl = createTitle("INGRESOS TOTALES");

        double incomes = calculateSum("precio_pagado");
        double fines = calculateSum("multa");
        double total = incomes + fines;

        VBox summary = new VBox(10);
        summary.setAlignment(Pos.CENTER);
        summary.setPadding(new Insets(20));
        summary.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 10;");
        summary.setMaxWidth(400);

        summary.getChildren().addAll(
                createResumenLabel("Ingresos por alquileres:", incomes),
                createResumenLabel("Multas registradas:", fines),
                createResumenLabel("TOTAL:", total));

        centralPanel.getChildren().addAll(lbl, summary);
    }

    private ObservableList<MovieRow> loadMovies() {
        ObservableList<MovieRow> movies = FXCollections.observableArrayList();

        String sql = """
                SELECT p.id, p.titulo, p.director, p.ano, g.nombre AS genero, p.precio, p.disponible
                FROM pelicula p
                JOIN genero g ON p.id_genero = g.id
                ORDER BY p.id ASC
                """;

        Connection conn = DatabaseManager.getInstance().connectToDb();

        if (conn == null) {
            return movies;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                movies.add(new MovieRow(
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

        return movies;
    }

    private ObservableList<RentalRow> loadRentals() {
        ObservableList<RentalRow> rentals = FXCollections.observableArrayList();

        String sql = """
                SELECT id, usuario, pelicula, fecha_alquiler, fecha_devolucion,
                       estado, precio_pagado, multa_actual
                FROM v_alquileres
                ORDER BY fecha_alquiler DESC
                """;

        Connection conn = DatabaseManager.getInstance().connectToDb();

        if (conn == null) {
            return rentals;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rentals.add(new RentalRow(
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

        return rentals;
    }

    private ObservableList<UserRow> loadUsers() {
        ObservableList<UserRow> userRows = FXCollections.observableArrayList();
        try {
            for (User u : new UserFunctions().getUsers()) {
                userRows.add(new UserRow(u.getId(), u.getNombre(), u.getEmail(), u.getRol()));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "No se pudieron cargar los usuarios.");
            e.printStackTrace();
        }
        return userRows;
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

    private void deleteMovie(int idPelicula) {
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

    private double calculateSum(String columna) {
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

    private void selectFile(TextField destination, String windowTitle) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(windowTitle);

        File file = fileChooser.showOpenDialog(getScene().getWindow());

        if (file != null) {
            destination.setText(file.getPath());
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

    private Label createResumenLabel(String text, double value) {
        Label lbl = new Label(text + " " + String.format("%.2f EUR", value));
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

    public static class MovieRow {
        private int id;
        private String titulo;
        private String director;
        private int ano;
        private String genero;
        private double precio;
        private String disponible;

        public MovieRow(int id, String titulo, String director, int ano, String genero, double precio,
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

    public static class RentalRow {
        private int id;
        private String usuario;
        private String pelicula;
        private String fechaAlquiler;
        private String fechaDevolucion;
        private String estado;
        private double precioPagado;
        private double multaActual;

        public RentalRow(int id, String usuario, String pelicula, String fechaAlquiler,
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

    public static class UserRow {
        private int id;
        private String nombre;
        private String email;
        private String rol;

        public UserRow(int id, String nombre, String email, String rol) {
            this.id = id;
            this.nombre = nombre;
            this.email = email;
            this.rol = rol;
        }

        public int getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        public String getEmail() {
            return email;
        }

        public String getRol() {
            return rol;
        }
    }
}