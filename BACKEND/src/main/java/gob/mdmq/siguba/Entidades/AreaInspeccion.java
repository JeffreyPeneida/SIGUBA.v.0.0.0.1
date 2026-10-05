package gob.mdmq.siguba.Entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import jakarta.persistence.*;
import lombok.ToString;

@Entity
@Table(name = "UBA_AREA_INSPECCION", schema = "PROYECTO_UBA")
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class AreaInspeccion implements Serializable {

    private static final long serialVersionUID = 1L;

    public AreaInspeccion() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_AREA")
    private Long idArea;

    @Column(name = "NOMBRE", nullable = false, length = 150)
    private String nombre;

    public AreaInspeccion(Long idArea, String nombre) {
        this.idArea = idArea;
        this.nombre = nombre;
    }

    public Long getIdArea() {
        return idArea;
    }

    public void setIdArea(Long idArea) {
        this.idArea = idArea;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

}