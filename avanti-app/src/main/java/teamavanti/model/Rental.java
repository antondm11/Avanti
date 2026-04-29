package teamavanti.model;

import java.util.Date;

public class Rental {
    // Atributos
    private int id;
    private int movieId;
    private int userId;
    private Date fechaAlquiler;
    private Date fechaDevolucion;
    private double precioAlquiler;

    // Constructor vacío por defecto *Opcional*
    public Rental() {
    }

    // Constructor con parámetros
    public Rental(int id, int movieId, int userId, Date fechaAlquiler, Date fechaDevolucion, double precioAlquiler) {
        this.id = id;
        this.movieId = movieId;
        this.userId = userId;
        this.fechaAlquiler = fechaAlquiler;
        this.fechaDevolucion = fechaDevolucion;
        this.precioAlquiler = precioAlquiler;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getFechaAlquiler() {
        return fechaAlquiler;
    }

    public void setFechaAlquiler(Date fechaAlquiler) {
        this.fechaAlquiler = fechaAlquiler;
    }

    public Date getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(Date fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public double getPrecioAlquiler() {
        return precioAlquiler;
    }

    public void setPrecioAlquiler(double precioAlquiler) {
        this.precioAlquiler = precioAlquiler;
    }

    // Método ToString *Útil para la depuración*
    @Override
    public String toString() {
        return "Rental [id=" + id + ", movieId=" + movieId + ", userId=" + userId + ", fechaAlquiler="
                + fechaAlquiler + ", fechaDevolucion=" + fechaDevolucion + ", precioAlquiler=" + precioAlquiler
                + "]";
    }

}
