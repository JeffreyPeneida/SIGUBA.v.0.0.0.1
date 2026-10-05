package gob.mdmq.siguba.Entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import jakarta.persistence.*;
import lombok.ToString;

@Entity
@Table(name = "UBA_TIPO_PARROQUIA", schema = "PROYECTO_UBA")
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class TipoParroquia implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TIPO_PARROQUIA")
    private Long idTipoParroquia;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    public TipoParroquia() {
    }

    public TipoParroquia(Long idTipoParroquia, String nombre) {
        this.idTipoParroquia = idTipoParroquia;
        this.nombre = nombre;
    }

    public Long getIdTipoParroquia() {
        return idTipoParroquia;
    }

    public void setIdTipoParroquia(Long idTipoParroquia) {
        this.idTipoParroquia = idTipoParroquia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}