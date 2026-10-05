package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoTipoEvidencia {

    private Long idEvidencia;
    private String nombre;
    private Long idEspecie;
    private String nombreEspecie;

    public DtoTipoEvidencia() {
    }

    public DtoTipoEvidencia(Long idEvidencia, String nombre) {
        this.idEvidencia = idEvidencia;
        this.nombre = nombre;
    }

    public DtoTipoEvidencia(Long idEvidencia, String nombre, Long idEspecie, String nombreEspecie) {
        this.idEvidencia = idEvidencia;
        this.nombre = nombre;
        this.idEspecie = idEspecie;
        this.nombreEspecie = nombreEspecie;
    }

    public Long getIdEvidencia() {
        return idEvidencia;
    }

    public void setIdEvidencia(Long idEvidencia) {
        this.idEvidencia = idEvidencia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getIdEspecie() {
        return idEspecie;
    }

    public void setIdEspecie(Long idEspecie) {
        this.idEspecie = idEspecie;
    }

    public String getNombreEspecie() {
        return nombreEspecie;
    }

    public void setNombreEspecie(String nombreEspecie) {
        this.nombreEspecie = nombreEspecie;
    }
}