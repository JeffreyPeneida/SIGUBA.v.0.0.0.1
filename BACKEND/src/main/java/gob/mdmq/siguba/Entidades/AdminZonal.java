package gob.mdmq.siguba.Entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import jakarta.persistence.*;
import lombok.ToString;

@Entity
@Table(name = "UBA_ADMIN_ZONAL", schema = "PROYECTO_UBA")
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class AdminZonal implements Serializable {

    private static final long serialVersionUID = 1L;

    public AdminZonal() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ADMIN_ZONAL")
    private Long idAdminZonal;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    public AdminZonal(Long idAdminZonal, String nombre) {
        this.idAdminZonal = idAdminZonal;
        this.nombre = nombre;
    }

    public Long getIdAdminZonal() {
        return idAdminZonal;
    }

    public void setIdAdminZonal(Long idAdminZonal) {
        this.idAdminZonal = idAdminZonal;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}