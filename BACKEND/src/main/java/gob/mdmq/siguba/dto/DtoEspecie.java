package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoEspecie implements Comparable<DtoEspecie> {

    private Long idEspecie;

    private String nombre;

    public DtoEspecie() {
    }

    public DtoEspecie(
            Long idEspecie,
            String nombre) {

        this.idEspecie = idEspecie;
        this.nombre = nombre;
    }

    public Long getIdEspecie() {
        return idEspecie;
    }

    public void setIdEspecie(Long idEspecie) {
        this.idEspecie = idEspecie;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public int compareTo(DtoEspecie o) {

        if (o == null) {
            return 1;
        }

        if (this.idEspecie == null && o.idEspecie == null) {
            return 0;
        }

        if (this.idEspecie == null) {
            return -1;
        }

        if (o.idEspecie == null) {
            return 1;
        }

        return this.idEspecie.compareTo(o.idEspecie);
    }

}