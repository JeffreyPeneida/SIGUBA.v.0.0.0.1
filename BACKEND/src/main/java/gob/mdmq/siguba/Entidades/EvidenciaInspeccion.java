package gob.mdmq.siguba.Entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Entity
@Table(name = "UBA_EVIDENCIA_INSPECCION", schema = "PROYECTO_UBA")
@ToString(exclude = {"inspeccion", "tipoEvidencia"})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class EvidenciaInspeccion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_INSPECCION")
    private Inspeccion inspeccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "ID_EVIDENCIA",
        foreignKey = @ForeignKey(name = "FK_EVIDENCIA_INSPECCION_TIPO")
    )
    private TipoEvidencia tipoEvidencia;

    @Column(name = "DESCRIPCION_OTRO", length = 255)
    private String descripcionOtro;

    public EvidenciaInspeccion(
            Long id,
            Inspeccion inspeccion,
            TipoEvidencia tipoEvidencia,
            String descripcionOtro) {

        this.id = id;
        this.inspeccion = inspeccion;
        this.tipoEvidencia = tipoEvidencia;
        this.descripcionOtro = descripcionOtro;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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