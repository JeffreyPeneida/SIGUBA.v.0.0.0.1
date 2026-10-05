package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoMotivoInspeccion implements Comparable<DtoMotivoInspeccion> {

    private Long idMotivo;
    private String nombre;

    public DtoMotivoInspeccion() {
    }

    public DtoMotivoInspeccion(Long idMotivo, String nombre) {
        this.idMotivo = idMotivo;
        this.nombre = nombre;
    }

    public Long getIdMotivo() {
        return idMotivo;
    }

    public void setIdMotivo(Long idMotivo) {
        this.idMotivo = idMotivo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public int compareTo(DtoMotivoInspeccion o) {

        if (o == null) {
            return 1;
        }

        if (this.idMotivo == null && o.idMotivo == null) {
            return 0;
        }

        if (this.idMotivo == null) {
            return -1;
        }

        if (o.idMotivo == null) {
            return 1;
        }

        return this.idMotivo.compareTo(o.idMotivo);
    }
}