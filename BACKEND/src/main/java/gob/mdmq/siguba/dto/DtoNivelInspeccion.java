package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import gob.mdmq.siguba.Entidades.NivelInfestacion;
import gob.mdmq.siguba.Entidades.Tramite;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoNivelInspeccion implements Comparable<DtoNivelInspeccion> {

    private Long id;
    private Tramite tramite;
    private Integer tipo;
    private NivelInfestacion nivelInfestacion;

    public DtoNivelInspeccion() {
    }

    public DtoNivelInspeccion(Long id,
                              Tramite tramite,
                              Integer tipo,
                              NivelInfestacion nivelInfestacion) {

        this.id = id;
        this.tramite = tramite;
        this.tipo = tipo;
        this.nivelInfestacion = nivelInfestacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tramite getTramite() {
        return tramite;
    }

    public void setTramite(Tramite tramite) {
        this.tramite = tramite;
    }

    public Integer getTipo() {
        return tipo;
    }

    public void setTipo(Integer tipo) {
        this.tipo = tipo;
    }

    public NivelInfestacion getNivelInfestacion() {
        return nivelInfestacion;
    }

    public void setNivelInfestacion(NivelInfestacion nivelInfestacion) {
        this.nivelInfestacion = nivelInfestacion;
    }

    @Override
    public int compareTo(DtoNivelInspeccion o) {

        if (o == null) {
            return 1;
        }

        if (this.id == null && o.id == null) {
            return 0;
        }

        if (this.id == null) {
            return -1;
        }

        if (o.id == null) {
            return 1;
        }

        return this.id.compareTo(o.id);
    }
}