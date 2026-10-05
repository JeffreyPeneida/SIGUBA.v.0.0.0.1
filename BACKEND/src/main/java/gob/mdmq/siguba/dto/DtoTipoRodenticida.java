package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoTipoRodenticida {

    private Long idTipo;
    private String nombre;

    public DtoTipoRodenticida() {
    }

    public DtoTipoRodenticida(Long idTipo, String nombre) {
        this.idTipo = idTipo;
        this.nombre = nombre;
    }

    public Long getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(Long idTipo) {
        this.idTipo = idTipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}