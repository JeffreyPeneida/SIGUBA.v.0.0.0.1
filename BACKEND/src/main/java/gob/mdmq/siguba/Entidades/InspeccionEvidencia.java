package gob.mdmq.siguba.Entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import jakarta.persistence.*;
import lombok.ToString;

@Entity
@IdClass(InspeccionEvidenciaPK.class)
@Table(name = "UBA_INSPECCION_EVIDENCIA", schema = "PROYECTO_UBA")
@ToString(exclude = {"inspeccion", "tipoEvidencia"})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class InspeccionEvidencia implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "ID_INSPECCION",
        foreignKey = @ForeignKey(name = "FK_IE_INSPECCION")
    )
    private Inspeccion inspeccion;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "ID_EVIDENCIA",
        foreignKey = @ForeignKey(name = "FK_IE_EVIDENCIA")
    )
    private TipoEvidencia tipoEvidencia;

    @Column(name = "DESCRIPCION_OTRO", length = 20)
    private String descripcionOtro;

    public InspeccionEvidencia() {
    }

    public InspeccionEvidencia(
            Inspeccion inspeccion,
            TipoEvidencia tipoEvidencia,
            String descripcionOtro) {

        this.inspeccion = inspeccion;
        this.tipoEvidencia = tipoEvidencia;
        this.descripcionOtro = descripcionOtro;
    }

    public Inspeccion getInspeccion() {
        return inspeccion;
    }

    public void setInspeccion(Inspeccion inspeccion) {
        this.inspeccion = inspeccion;
    }

    public TipoEvidencia getTipoEvidencia() {
        return tipoEvidencia;
    }

    public void setTipoEvidencia(TipoEvidencia tipoEvidencia) {
        this.tipoEvidencia = tipoEvidencia;
    }

    public String getDescripcionOtro() {
        return descripcionOtro;
    }

    public void setDescripcionOtro(String descripcionOtro) {
        this.descripcionOtro = descripcionOtro;
    }
}