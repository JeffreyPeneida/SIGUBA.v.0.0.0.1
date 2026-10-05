package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.ToString;

@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoAdminZonal {

    private Long idAdminZonal;
    private String nombre;

    public DtoAdminZonal() {}

    public DtoAdminZonal(Long idAdminZonal, String nombre) {
        this.idAdminZonal = idAdminZonal;
        this.nombre = nombre;
    }

    public Long getIdAdminZonal() {
        return idAdminZonal;
    }

    public void setIdAdminZonal(Long idAdminZonal) {
        this.idAdminZonal = idAdminZonal;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}