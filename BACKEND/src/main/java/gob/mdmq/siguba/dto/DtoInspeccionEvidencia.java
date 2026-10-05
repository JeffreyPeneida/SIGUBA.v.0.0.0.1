package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoInspeccionEvidencia
        implements Comparable<DtoInspeccionEvidencia> {

    private Long idInspeccion;

    private Long idEvidencia;

    private String tipoEvidencia;

    private String descripcionOtro;

    public DtoInspeccionEvidencia() {
    }

    public DtoInspeccionEvidencia(
            Long idInspeccion,
            Long idEvidencia,
            String tipoEvidencia,
            String descripcionOtro) {

        this.idInspeccion = idInspeccion;
        this.idEvidencia = idEvidencia;
        this.tipoEvidencia = tipoEvidencia;
        this.descripcionOtro = descripcionOtro;
    }

    public Long getIdInspeccion() {
        return idInspeccion;
    }

    public void setIdInspeccion(Long idInspeccion) {
        this.idInspeccion = idInspeccion;
    }

    public Long getIdEvidencia() {
        return idEvidencia;
    }

    public void setIdEvidencia(Long idEvidencia) {
        this.idEvidencia = idEvidencia;
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
    public int compareTo(DtoInspeccionEvidencia o) {

        if (o == null) {
            return 1;
        }

        if (this.idInspeccion == null && o.idInspeccion == null) {
            return 0;
        }

        if (this.idInspeccion == null) {
            return -1;
        }

        if (o.idInspeccion == null) {
            return 1;
        }

        return this.idInspeccion.compareTo(o.idInspeccion);
    }
}