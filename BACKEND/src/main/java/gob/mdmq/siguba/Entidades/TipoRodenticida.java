package gob.mdmq.siguba.Entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.List;
import jakarta.persistence.*;
import lombok.ToString;

@Entity
@Table(name = "UBA_TIPO_RODENTICIDA", schema = "PROYECTO_UBA")
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class TipoRodenticida implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TIPO")
    private Long idTipo;

    @Column(name = "NOMBRE", nullable = false, unique = true, length = 100)
    private String nombre;

    @JsonIgnore
    @OneToMany(mappedBy = "tipoRodenticida", fetch = FetchType.LAZY)
    private List<UsoRodenticida> usosRodenticida;

    public TipoRodenticida() {
    }

    public TipoRodenticida(Long idTipo, String nombre) {
        this.idTipo = idTipo;
        this.nombre = nombre;
    }

    public Long getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(Long idTipo) {
        this.idTipo = idTipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<UsoRodenticida> getUsosRodenticida() {
        return usosRodenticida;
    }

    public void setUsosRodenticida(List<UsoRodenticida> usosRodenticida) {
        this.usosRodenticida = usosRodenticida;
    }
}