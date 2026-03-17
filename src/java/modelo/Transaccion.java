package modelo;

public class Transaccion {

    private int idTransaccion;
    private double monto;
    private String descripcion;
    private String fecha;
    private int idUsuario;
    private int categoria_id;

    public Transaccion() {
    }

    // Constructor para insertar datos nuevos
    public Transaccion(double monto, String descripcion, String fecha, int id, int categoria_id) {
        this.monto = monto;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.idUsuario = id;
        this.categoria_id = categoria_id;
    }

    public int getIdTransaccion() {
        return idTransaccion;
    }

    public void setIdTransaccion(int idTransaccion) {
        this.idTransaccion = idTransaccion;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getCategoria_id() {
        return categoria_id;
    }

    public void setCategoria_id(int categoria_id) {
        this.categoria_id = categoria_id;
    }

}
