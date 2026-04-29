package teamavanti.model;

public class Movie {
    // Atributos
    int id;
    String titulo;
    String director;
    int ano;
    int stock;
    double precio;

    // Constructor vacío por defecto *Opcional*
    public Movie() {
    }

    // Constructor con parámetros
    public Movie(int id, String titulo, String director, int ano, int stock, double precio) {
        this.id = id;
        this.titulo = titulo;
        this.director = director;
        this.ano = ano;
        this.stock = stock;
        this.precio = precio;
    }

    // Getters y Setters
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

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    // Método ToString
    @Override
    public String toString() {
        return "Movie [id=" + id + ", titulo=" + titulo + ", director=" + director + ", ano=" + ano + ", stock="
                + stock + ", precio=" + precio + "]";
    }

}
