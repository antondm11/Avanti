package teamavanti.model;

import java.time.LocalDate;

public class Rental {

    private int id;
    private LocalDate fechaAlquiler;
    private LocalDate fechaDevolucion; // null si no se ha devuelto
    private String estado; // "ACTIVO", "VENCIDO", "DEVUELTO"
    private double precio; // precio pagado (alias de precioPagado)
    private double multa;
    private int idPelicula;
    private int idUsuario;

    // Objeto película asociado (para mostrar en la UI sin JOIN extra)
    private Movie pelicula;

    // Constructor vacío
    public Rental() {
    }

    // Constructor completo
    public Rental(int id, LocalDate fechaAlquiler, LocalDate fechaDevolucion,
            String estado, double precio, double multa,
            int idPelicula, int idUsuario) {
        this.id = id;
        this.fechaAlquiler = fechaAlquiler;
        this.fechaDevolucion = fechaDevolucion;
        this.estado = estado;
        this.precio = precio;
        this.multa = multa;
        this.idPelicula = idPelicula;
        this.idUsuario = idUsuario;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getFechaAlquiler() {
        return fechaAlquiler;
    }

    public void setFechaAlquiler(LocalDate fechaAlquiler) {
        this.fechaAlquiler = fechaAlquiler;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    // Alias para compatibilidad con código que usa precioPagado
    public double getPrecioPagado() {
        return precio;
    }

    public void setPrecioPagado(double precioPagado) {
        this.precio = precioPagado;
    }

    public double getMulta() {
        return multa;
    }

    public void setMulta(double multa) {
        this.multa = multa;
    }

    public int getIdPelicula() {
        return idPelicula;
    }

    public void setIdPelicula(int idPelicula) {
        this.idPelicula = idPelicula;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Movie getPelicula() {
        return pelicula;
    }

    public void setPelicula(Movie pelicula) {
        this.pelicula = pelicula;
    }

    @Override
    public String toString() {
        return "Rental [id=" + id + ", estado=" + estado + ", fechaAlquiler=" + fechaAlquiler
                + ", precio=" + precio + ", multa=" + multa + "]";
    }
}
