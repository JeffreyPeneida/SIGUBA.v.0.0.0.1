package gob.mdmq.siguba.Entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "UBA_USO_RODENTICIDA", schema = "PROYECTO_UBA")
@ToString(exclude = {"tramite", "tipoRodenticida"})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class UsoRodenticida implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USO")
    private Long idUso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CODIGO_INTERNO")
    private Tramite tramite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO")
    private TipoRodenticida tipoRodenticida;

    @Column(name = "CANTIDAD")
    private Integer cantidad;

    public UsoRodenticida() {
    }

    public UsoRodenticida(Long idUso, Tramite tramite,
                          TipoRodenticida tipoRodenticida,
                          Integer cantidad) {
        this.idUso = idUso;
        this.tramite = tramite;
        this.tipoRodenticida = tipoRodenticida;
        this.cantidad = cantidad;
    }

    public Long getIdUso() {
        return idUso;
    }

    public void setIdUso(Long idUso) {
        this.idUso = idUso;
    }

    public Tramite getTramite() {
        return tramite;
    }

    public void setTramite(Tramite tramite) {
        this.tramite = tramite;
    }

    public TipoRodenticida getTipoRodenticida() {
        return tipoRodenticida;
    }

    public void setTipoRodenticida(TipoRodenticida tipoRodenticida) {
        this.tipoRodenticida = tipoRodenticida;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}