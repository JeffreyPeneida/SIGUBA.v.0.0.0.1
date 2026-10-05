package gob.mdmq.siguba.Entidades;

import java.io.Serializable;
import java.util.Objects;

public class InspeccionEvidenciaPK implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long inspeccion;
    private Long tipoEvidencia;

    public InspeccionEvidenciaPK() {
    }

    public InspeccionEvidenciaPK(Long inspeccion, Long tipoEvidencia) {
        this.inspeccion = inspeccion;
        this.tipoEvidencia = tipoEvidencia;
    }

    public Long getInspeccion() {
        return inspeccion;
    }

    public void setInspeccion(Long inspeccion) {
        this.inspeccion = inspeccion;
    }

    public Long getTipoEvidencia() {
        return tipoEvidencia;
    }

    public void setTipoEvidencia(Long tipoEvidencia) {
        this.tipoEvidencia = tipoEvidencia;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InspeccionEvidenciaPK)) return false;
        InspeccionEvidenciaPK that = (InspeccionEvidenciaPK) o;
        return Objects.equals(inspeccion, that.inspeccion) &&
               Objects.equals(tipoEvidencia, that.tipoEvidencia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inspeccion, tipoEvidencia);
    }
}