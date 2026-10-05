package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import gob.mdmq.siguba.Entidades.Especie;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoNivelInfestacion implements Comparable<DtoNivelInfestacion> {

    private Long idNivel;
    private Especie especie;
    private String nombre;
    private String descripcion;

    public DtoNivelInfestacion() {
    }

    public DtoNivelInfestacion(Long idNivel,
                               Especie especie,
                               String nombre,
                               String descripcion) {

        this.idNivel = idNivel;
        this.especie = especie;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Long getIdNivel() {
        return idNivel;
    }

    public void setIdNivel(Long idNivel) {
        this.idNivel = idNivel;
    }

    public Especie getEspecie() {
        return especie;
    }

    public void setEspecie(Especie especie) {
        this.especie = especie;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public int compareTo(DtoNivelInfestacion o) {

        if (o == null) {
            return 1;
        }

        if (this.idNivel == null && o.idNivel == null) {
            return 0;
        }

        if (this.idNivel == null) {
            return -1;
        }

        if (o.idNivel == null) {
            return 1;
        }

        return this.idNivel.compareTo(o.idNivel);
    }
}