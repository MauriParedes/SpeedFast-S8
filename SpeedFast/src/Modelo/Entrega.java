package Modelo;

import java.sql.Date;
import java.sql.Time;


public class Entrega {
    private int id;
    private int idPedido;
    private int idRepartidor;
    private Date fecha;
    private Time hora;
    private String descripcionPedido;   // solo lectura (JOIN)
    private String nombreRepartidor;    // solo lectura (JOIN)

    public Entrega() {}

    public Entrega(int id, int idPedido, int idRepartidor, Date fecha, Time hora) {
        this.id = id;
        this.idPedido = idPedido;
        this.idRepartidor = idRepartidor;
        this.fecha = fecha;
        this.hora = hora;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }
    public int getIdRepartidor() { return idRepartidor; }
    public void setIdRepartidor(int idRepartidor) { this.idRepartidor = idRepartidor; }
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    public Time getHora() { return hora; }
    public void setHora(Time hora) { this.hora = hora; }
    public String getDescripcionPedido() { return descripcionPedido; }
    public void setDescripcionPedido(String d) { this.descripcionPedido = d; }
    public String getNombreRepartidor() { return nombreRepartidor; }
    public void setNombreRepartidor(String n) { this.nombreRepartidor = n; }
}
