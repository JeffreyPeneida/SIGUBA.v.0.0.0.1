package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoBarrio implements Comparable<DtoBarrio> {

    private Long idBarrio;
    private String nombre;
    private Long idParroquia;
    private String parroquia;

    public DtoBarrio() {
    }

    public DtoBarrio(
            Long idBarrio,
            String nombre,
            Long idParroquia,
            String parroquia) {

        this.idBarrio = idBarrio;
        this.nombre = nombre;
        this.idParroquia = idParroquia;
        this.parroquia = parroquia;
    }

    public Long getIdBarrio() {
        return idBarrio;
    }

    public void setIdBarrio(Long idBarrio) {
        this.idBarrio = idBarrio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getIdParroquia() {
        return idParroquia;
    }

    public void setIdParroquia(Long idParroquia) {
        this.idParroquia = idParroquia;
    }

    public String getParroquia() {
        return parroquia;
    }

    public void setParroquia(String parroquia) {
        this.parroquia = parroquia;
    }

    @Override
    public int compareTo(DtoBarrio o) {

        if (o == null) {
            return 1;
        }

        if (this.idBarrio == null && o.idBarrio == null) {
            return 0;
        }

        if (this.idBarrio == null) {
            return -1;
        }

        if (o.idBarrio == null) {
            return 1;
        }

        return this.idBarrio.compareTo(o.idBarrio);
    }
}