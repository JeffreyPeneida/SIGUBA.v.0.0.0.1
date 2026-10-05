package gob.mdmq.siguba.Entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Entity
@Table(name = "UBA_DEPENDENCIA", schema = "PROYECTO_UBA")
@ToString(exclude = "tipoDenunciante")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Dependencia implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DEPENDENCIA")
    private Long idDependencia;

    @Column(name = "NOMBRE", length = 150)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "ID_TIPO",
        foreignKey = @ForeignKey(name = "FK_DEPENDENCIA_TIPO")
    )
    private TipoDenunciante tipoDenunciante;

    public Dependencia(
            Long idDependencia,
            String nombre,
            TipoDenunciante tipoDenunciante) {

        this.idDependencia = idDependencia;
        this.nombre = nombre;
        this.tipoDenunciante = tipoDenunciante;
    }

    public Long getIdDependencia() {
        return idDependencia;
    }

    public void setIdDependencia(Long idDependencia) {
        this.idDependencia = idDependencia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoDenunciante getTipoDenunciante() {
        return tipoDenunciante;
    }

    public void setTipoDenunciante(TipoDenunciante tipoDenunciante) {
        this.tipoDenunciante = tipoDenunciante;
    }
}