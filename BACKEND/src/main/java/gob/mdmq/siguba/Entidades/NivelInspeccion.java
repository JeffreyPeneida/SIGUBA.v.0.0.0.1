package gob.mdmq.siguba.Entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import jakarta.persistence.*;
import lombok.ToString;

@Entity
@Table(name = "UBA_NIVEL_INSPECCION", schema = "PROYECTO_UBA")
@ToString(exclude = {"tramite", "nivelInfestacion"})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class NivelInspeccion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "ID_CODIGO_INTERNO",
        foreignKey = @ForeignKey(name = "FK_NI_TRAMITE")
    )
    private Tramite tramite;

    @Column(name = "TIPO")
    private Integer tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "ID_NIVEL",
        foreignKey = @ForeignKey(name = "FK_NI_NIVEL")
    )
    private NivelInfestacion nivelInfestacion;

    public NivelInspeccion() {
    }

    public NivelInspeccion(Long id,
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
}