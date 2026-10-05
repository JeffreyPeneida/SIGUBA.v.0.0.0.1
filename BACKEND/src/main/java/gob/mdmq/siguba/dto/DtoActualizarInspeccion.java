package gob.mdmq.siguba.dto;

import java.util.Date;

public class DtoActualizarInspeccion {

    private Date fechaInspeccion;
    private String horaInspeccion;
    private Long idArea;
    private Long idMotivo;
    private String descripcion;
    private String conclusiones;
    private String recomendaciones;
    private Long idRegistra;
    private Long idCodigoInterno;

    public DtoActualizarInspeccion() {
    }

    public Date getFechaInspeccion() {
        return fechaInspeccion;
    }

    public void setFechaInspeccion(Date fechaInspeccion) {
        this.fechaInspeccion = fechaInspeccion;
    }

    public String getHoraInspeccion() {
        return horaInspeccion;
    }

    public void setHoraInspeccion(String horaInspeccion) {
        this.horaInspeccion = horaInspeccion;
    }

    public Long getIdArea() {
        return idArea;
    }

    public void setIdArea(Long idArea) {
        this.idArea = idArea;
    }

    public Long getIdMotivo() {
        return idMotivo;
    }

    public void setIdMotivo(Long idMotivo) {
        this.idMotivo = idMotivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getConclusiones() {
        return conclusiones;
    }

    public void setConclusiones(String conclusiones) {
        this.conclusiones = conclusiones;
    }

    public String getRecomendaciones() {
        return recomendaciones;
    }

    public void setRecomendaciones(String recomendaciones) {
        this.recomendaciones = recomendaciones;
    }

    public Long getIdRegistra() {
        return idRegistra;
    }

    public void setIdRegistra(Long idRegistra) {
        this.idRegistra = idRegistra;
    }

    public Long getIdCodigoInterno() {
        return idCodigoInterno;
    }

    public void setIdCodigoInterno(Long idCodigoInterno) {
        this.idCodigoInterno = idCodigoInterno;
    }
}