package teamavanti.model;

public class Movie {

    private int id;
    private String titulo;
    private String director;
    private int ano;
    private String sinopsis;
    private int duracion;
    private double precio;
    private String imagen;
    private String video;
    private boolean disponible;

    private int idGenero;
    private String genero;

    public Movie() {
    }

    public Movie(int id, String titulo, String director, int ano, String sinopsis,
            int duracion, double precio, String imagen, String video,
            boolean disponible, int idGenero, String genero) {
        this.id = id;
        this.titulo = titulo;
        this.director = director;
        this.ano = ano;
        this.sinopsis = sinopsis;
        this.duracion = duracion;
        this.precio = precio;
        this.imagen = imagen;
        this.video = video;
        this.disponible = disponible;
        this.idGenero = idGenero;
        this.genero = genero;
    }

    public Movie(int id, String titulo, String director, int ano, String sinopsis,
            int duracion, double precio, String imagen, String video,
            boolean disponible, int idGenero) {
        this(id, titulo, director, ano, sinopsis, duracion, precio, imagen, video, disponible, idGenero, "");
    }

    public Movie(int id, String titulo, String director, int ano, int duracion, double precio) {
        this(id, titulo, director, ano, "", duracion, precio, "", "", true, 1, "");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public void setSinopsis(String sinopsis) {
        this.sinopsis = sinopsis;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    // Alias para código anterior/nuevo
    public String getUrlImagen() {
        return imagen;
    }

    public void setUrlImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    // Alias para código anterior/nuevo
    public String getUrlVideo() {
        return video;
    }

    public void setUrlVideo(String video) {
        this.video = video;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public int getIdGenero() {
        return idGenero;
    }

    public void setIdGenero(int idGenero) {
        this.idGenero = idGenero;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    @Override
    public String toString() {
        return titulo + " (" + ano + ") - " + director;
    }
}