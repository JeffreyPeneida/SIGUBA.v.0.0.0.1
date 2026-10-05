package gob.mdmq.siguba.Entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Entity
@Table(name = "UBA_INSPECCION", schema = "PROYECTO_UBA")
@ToString(exclude = {"especie", "nivelInfestacion"})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Inspeccion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_INSPECCION")
    private Long idInspeccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "ID_ESPECIE",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_INSPECCION_ESPECIE")
    )
    private Especie especie;

    @Column(name = "AREAS_SUPERVISADAS")
    private String areasSupervisadas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "ID_NIVEL",
        foreignKey = @ForeignKey(name = "FK_INSPECCION_NIVEL")
    )
    private NivelInfestacion nivelInfestacion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA")
    private Date fecha;

    public Inspeccion(Long idInspeccion,
                      Especie especie,
                      String areasSupervisadas,
                      NivelInfestacion nivelInfestacion,
                      Date fecha) {

        this.idInspeccion = idInspeccion;
        this.especie = especie;
        this.areasSupervisadas = areasSupervisadas;
        this.nivelInfestacion = nivelInfestacion;
        this.fecha = fecha;
    }

    public Long getIdInspeccion() {
        return idInspeccion;
    }

    public void setIdInspeccion(Long idInspeccion) {
        this.idInspeccion = idInspeccion;
    }

    public Especie getEspecie() {
        return especie;
    }

    public void setEspecie(Especie especie) {
        this.especie = especie;
    }

    public String getAreasSupervisadas() {
        return areasSupervisadas;
    }

    public void setAreasSupervisadas(String areasSupervisadas) {
        this.areasSupervisadas = areasSupervisadas;
    }

    public NivelInfestacion getNivelInfestacion() {
        return nivelInfestacion;
    }

    public void setNivelInfestacion(NivelInfestacion nivelInfestacion) {
        this.nivelInfestacion = nivelInfestacion;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
}