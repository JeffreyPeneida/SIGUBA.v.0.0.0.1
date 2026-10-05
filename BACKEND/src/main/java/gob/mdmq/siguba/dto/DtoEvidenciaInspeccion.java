package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoEvidenciaInspeccion
        implements Comparable<DtoEvidenciaInspeccion> {

    private Long id;

    private Long idInspeccion;

    private String tipoEvidencia;

    private String descripcionOtro;

    public DtoEvidenciaInspeccion() {
    }

    public DtoEvidenciaInspeccion(
            Long id,
            Long idInspeccion,
            String tipoEvidencia,
            String descripcionOtro) {

        this.id = id;
        this.idInspeccion = idInspeccion;
        this.tipoEvidencia = tipoEvidencia;
        this.descripcionOtro = descripcionOtro;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdInspeccion() {
        return idInspeccion;
    }

    public void setIdInspeccion(Long idInspeccion) {
        this.idInspeccion = idInspeccion;
    }

    public String getTipoEvidencia() {
        return tipoEvidencia;
    }

    public void setTipoEvidencia(String tipoEvidencia) {
        this.tipoEvidencia = tipoEvidencia;
    }

    public String getDescripcionOtro() {
        return descripcionOtro;
    }

    public void setDescripcionOtro(String descripcionOtro) {
        this.descripcionOtro = descripcionOtro;
    }

    @Override
    public int compareTo(DtoEvidenciaInspeccion o) {

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

