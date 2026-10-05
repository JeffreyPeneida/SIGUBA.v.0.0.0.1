package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoTipoDenunciante implements Comparable<DtoTipoDenunciante> {

    private Long idTipo;
    private String nombre;

    public DtoTipoDenunciante() {
    }

    public DtoTipoDenunciante(Long idTipo, String nombre) {
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

    @Override
    public int compareTo(DtoTipoDenunciante o) {

        if (o == null) {
            return 1;
        }

        if (this.idTipo == null && o.idTipo == null) {
            return 0;
        }

        if (this.idTipo == null) {
            return -1;
        }

        if (o.idTipo == null) {
            return 1;
        }

        return this.idTipo.compareTo(o.idTipo);
    }
}