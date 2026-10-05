package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoAreaInspeccion implements Comparable<DtoAreaInspeccion> {

    private Long idArea;
    private String nombre;

    public DtoAreaInspeccion() {
    }

    public DtoAreaInspeccion(
            Long idArea,
            String nombre) {

        this.idArea = idArea;
        this.nombre = nombre;
    }

    public Long getIdArea() {
        return idArea;
    }

    public void setIdArea(Long idArea) {
        this.idArea = idArea;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public int compareTo(DtoAreaInspeccion o) {

        if (o == null) {
            return 1;
        }

        if (this.idArea == null && o.idArea == null) {
            return 0;
        }

        if (this.idArea == null) {
            return -1;
        }

        if (o.idArea == null) {
            return 1;
        }

        return this.idArea.compareTo(o.idArea);
    }

}