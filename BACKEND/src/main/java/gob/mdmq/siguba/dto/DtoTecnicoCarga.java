package gob.mdmq.siguba.dto;

/**
 * Tecnico con su carga de trabajo actual.
 *
 * Asignar sin ver cuantos tramites lleva cada uno es asignar a ciegas: quien
 * reparte necesita saber quien esta libre.
 */
public class DtoTecnicoCarga {

    private Long idTecnico;
    private String nombre;
    private String apellido;
    private String cedula;
    private String correo;

    /** Tramites asignados que todavia no tienen inspeccion registrada. */
    private long pendientes;

    /** Total historico asignado. */
    private long total;

    public DtoTecnicoCarga() {
    }

    public DtoTecnicoCarga(Long idTecnico, String nombre, String apellido,
                           String cedula, String correo, long pendientes, long total) {
        this.idTecnico = idTecnico;
        this.nombre = nombre;
        this.apellido = apellido;
        this.cedula = cedula;
        this.correo = correo;
        this.pendientes = pendientes;
        this.total = total;
    }

    public Long getIdTecnico() { return idTecnico; }
    public void setIdTecnico(Long v) { this.idTecnico = v; }

    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }

    public String getApellido() { return apellido; }
    public void setApellido(String v) { this.apellido = v; }

    public String getCedula() { return cedula; }
    public void setCedula(String v) { this.cedula = v; }

    public String getCorreo() { return correo; }
    public void setCorreo(String v) { this.correo = v; }

    public long getPendientes() { return pendientes; }
    public void setPendientes(long v) { this.pendientes = v; }

    public long getTotal() { return total; }
    public void setTotal(long v) { this.total = v; }
}
