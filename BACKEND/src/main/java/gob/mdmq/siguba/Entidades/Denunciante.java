package gob.mdmq.siguba.Entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Entity
@Table(name = "UBA_DENUNCIANTE", schema = "PROYECTO_UBA")
@ToString(exclude = "tipoDenunciante")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Denunciante implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DENUNCIANTE")
    private Long idDenunciante;

    @Column(name = "NOMBRE", length = 150)
    private String nombre;

    @Column(name = "APELLIDO", length = 150)
    private String apellido;

    @Column(name = "CEDULA", length = 20)
    private String cedula;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "TIPO_DENUNCIANTE_ID",
        foreignKey = @ForeignKey(name = "FK_DENUNCIANTE_TIPO")
    )
    private TipoDenunciante tipoDenunciante;

    @Column(name = "DIRECCION", length = 200)
    private String direccion;

    @Column(name = "REFERENCIA", length = 200)
    private String referencia;

    @Column(name = "TELEFONO", length = 20)
    private String telefono;

    public Denunciante(
            Long idDenunciante,
            String nombre,
            String apellido,
            String cedula,
            TipoDenunciante tipoDenunciante,
            String direccion,
            String referencia,
            String telefono) {

        this.idDenunciante = idDenunciante;
        this.nombre = nombre;
        this.apellido = apellido;
        this.cedula = cedula;
        this.tipoDenunciante = tipoDenunciante;
        this.direccion = direccion;
        this.referencia = referencia;
        this.telefono = telefono;
    }

    public Long getIdDenunciante() {
        return idDenunciante;
    }

    public void setIdDenunciante(Long idDenunciante) {
        this.idDenunciante = idDenunciante;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public TipoDenunciante getTipoDenunciante() {
        return tipoDenunciante;
    }

    public void setTipoDenunciante(TipoDenunciante tipoDenunciante) {
        this.tipoDenunciante = tipoDenunciante;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}