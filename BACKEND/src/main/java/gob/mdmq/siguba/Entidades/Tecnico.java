package gob.mdmq.siguba.Entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import jakarta.persistence.*;
import lombok.ToString;

@Entity
@Table(name = "UBA_TECNICO", schema = "PROYECTO_UBA")
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Tecnico implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TECNICO")
    private Long idTecnico;

    @Column(name = "NOMBRE", length = 150)
    private String nombre;

    @Column(name = "APELLIDO", length = 150)
    private String apellido;

    @Column(name = "CEDULA", length = 20)
    private String cedula;

    @Column(name = "TECNICO_USUARIO", length = 50)
    private String tecnicoUsuario;

    @Column(name = "PASSWORD", length = 255)
    private String password;

    @Column(name = "CORREO", length = 150)
    private String correo;

    @Column(name = "ESTADO", length = 10)
    private String estado;

    /**
     * Cuenta de UBA_USUARIO a la que pertenece esta ficha.
     *
     * Es el vinculo que faltaba entre quien puede iniciar sesion y quien puede
     * recibir tramites. Admite nulo por los tecnicos historicos que quedaron sin
     * cuenta, pero todo tecnico nuevo se crea ya enlazado.
     */
    @Column(name = "ID_USUARIO")
    private Integer idUsuario;

    public Tecnico() {
    }

    public Tecnico(Long idTecnico, String nombre, String apellido,
                   String cedula, String tecnicoUsuario,
                   String password, String correo, String estado) {

        this.idTecnico = idTecnico;
        this.nombre = nombre;
        this.apellido = apellido;
        this.cedula = cedula;
        this.tecnicoUsuario = tecnicoUsuario;
        this.password = password;
        this.correo = correo;
        this.estado = estado;
    }

    public Long getIdTecnico() {
        return idTecnico;
    }

    public void setIdTecnico(Long idTecnico) {
        this.idTecnico = idTecnico;
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

    public String getTecnicoUsuario() {
        return tecnicoUsuario;
    }

    public void setTecnicoUsuario(String tecnicoUsuario) {
        this.tecnicoUsuario = tecnicoUsuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
}