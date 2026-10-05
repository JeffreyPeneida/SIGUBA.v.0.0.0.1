package gob.mdmq.siguba.Entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Entity
@Table(name = "UBA_PARROQUIA", schema = "PROYECTO_UBA")
@ToString(exclude = "adminZonal")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Parroquia implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PARROQUIA")
    private Long idParroquia;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "ID_ADMIN_ZONAL",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_UBA_PARROQUIA_ADMIN_ZONAL")
    )
    private AdminZonal adminZonal;

    public Parroquia(Long idParroquia, String nombre, AdminZonal adminZonal) {
        this.idParroquia = idParroquia;
        this.nombre = nombre;
        this.adminZonal = adminZonal;
    }

    public Long getIdParroquia() {
        return idParroquia;
    }

    public void setIdParroquia(Long idParroquia) {
        this.idParroquia = idParroquia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public AdminZonal getAdminZonal() {
        return adminZonal;
    }

    public void setAdminZonal(AdminZonal adminZonal) {
        this.adminZonal = adminZonal;
    }
}