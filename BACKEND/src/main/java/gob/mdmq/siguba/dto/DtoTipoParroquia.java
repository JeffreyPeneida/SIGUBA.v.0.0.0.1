package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoTipoParroquia {

    private Long idTipoParroquia;
    private String nombre;

    public DtoTipoParroquia() {
    }

    public DtoTipoParroquia(Long idTipoParroquia, String nombre) {
        this.idTipoParroquia = idTipoParroquia;
        this.nombre = nombre;
    }

    public Long getIdTipoParroquia() {
        return idTipoParroquia;
    }

    public void setIdTipoParroquia(Long idTipoParroquia) {
        this.idTipoParroquia = idTipoParroquia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}