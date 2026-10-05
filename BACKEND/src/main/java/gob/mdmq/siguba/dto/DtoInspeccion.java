package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoInspeccion
        implements Comparable<DtoInspeccion> {

    private Long idInspeccion;

    private String especie;

    private String areasSupervisadas;

    private String nivelInfestacion;

    private Date fecha;

    public DtoInspeccion() {
    }

    public DtoInspeccion(
            Long idInspeccion,
            String especie,
            String areasSupervisadas,
            String nivelInfestacion,
            Date fecha) {

        this.idInspeccion = idInspeccion;
        this.especie = especie;
        this.areasSupervisadas = areasSupervisadas;
        this.nivelInfestacion = nivelInfestacion;
        this.fecha = fecha;
    }

    public Long getIdInspeccion() {
        return idInspeccion;
    }

    public void setIdInspeccion(Long idInspeccion) {
        this.idInspeccion = idInspeccion;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getAreasSupervisadas() {
        return areasSupervisadas;
    }

    public void setAreasSupervisadas(String areasSupervisadas) {
        this.areasSupervisadas = areasSupervisadas;
    }

    public String getNivelInfestacion() {
        return nivelInfestacion;
    }

    public void setNivelInfestacion(String nivelInfestacion) {
        this.nivelInfestacion = nivelInfestacion;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    @Override
    public int compareTo(DtoInspeccion o) {

        if (o == null) {
            return 1;
        }

        if (this.idInspeccion == null && o.idInspeccion == null) {
            return 0;
        }

        if (this.idInspeccion == null) {
            return -1;
        }

        if (o.idInspeccion == null) {
            return 1;
        }

        return this.idInspeccion.compareTo(o.idInspeccion);
    }

}