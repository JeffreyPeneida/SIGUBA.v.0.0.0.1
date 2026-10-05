package gob.mdmq.siguba.Entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.*;

@Entity
@Table(name = "UBA_USUARIO", schema = "PROYECTO_UBA")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USUARIO")
    private Integer idUsuario;

    @Column(name = "NOMBRE", length = 150)
    private String nombre;

    @Column(name = "APELLIDO", length = 150)
    private String apellido;

    @Column(name = "CEDULA", length = 20)
    private String cedula;

    @Temporal(TemporalType.DATE)
    @Column(name = "FECHA_NACIMIENTO")
    private Date fechaNacimiento;

    @Column(name = "USUARIO", length = 50)
    private String usuario;

    @Column(name = "MAIL", length = 150)
    private String mail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ADMIN_ZONAL")
    private AdminZonal adminZonal;

    @Column(name = "PASSWORD", length = 60)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROL", length = 20)
    private RolUsuario rol;

    @Column(name = "TIPO_IDENTIFICACION", nullable = false)
    private String tipoIdentificacion;

    @Column(name = "DENOMINACION", length = 80)
    private String denominacion;

    @Column(name = "MAIL_INSTITUCIONAL", length = 60)
    private String mailInstitucional;

    @Column(name = "DIRECCION", length = 200)
    private String direccion;

    @Column(name = "TELEFONO", length = 10)
    private String telefono;

    @Column(name = "MOVIL", length = 15)
    private String movil;

    @Column(name = "ACEPTACION_MEDIOS", length = 10)
    private String aceptacionMedios;

    @Column(name = "SIM", length = 30)
    private String sim;

    @Column(name = "SSO_ID", nullable = false, length = 100)
    private String ssoId;

    @Column(name = "TIPO_USUARIO", nullable = false)
    private Long tipoUsuario;

    @Column(name = "USUARIO_CREACION", length = 50)
    private String usuarioCreacion;

    @Column(name = "TERMINAL_CREACION", length = 20)
    private String terminalCreacion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_CREACION")
    private Date fechaCreacion;

    @Column(name = "USUARIO_MODIFICACION", length = 50)
    private String usuarioModificacion;

    @Column(name = "TERMINAL_MODIFICACION", length = 20)
    private String terminalModificacion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_MODIFICACION")
    private Date fechaModificacion;

    @Column(name = "ESTADO", length = 20)
    private String estado;

    public Usuario() {
    }

    public Usuario(Integer idUsuario, String nombre, String apellido, String cedula,
            Date fechaNacimiento, String usuario, String mail,
            AdminZonal adminZonal, String password, RolUsuario rol,
            String tipoIdentificacion, String denominacion,
            String mailInstitucional, String direccion,
            String telefono, String movil, String aceptacionMedios,
            String sim, String ssoId, Long tipoUsuario,
            String usuarioCreacion, String terminalCreacion,
            Date fechaCreacion, String usuarioModificacion,
            String terminalModificacion, Date fechaModificacion,
            String estado) {

        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.cedula = cedula;
        this.fechaNacimiento = fechaNacimiento;
        this.usuario = usuario;
        this.mail = mail;
        this.adminZonal = adminZonal;
        this.password = password;
        this.rol = rol;
        this.tipoIdentificacion = tipoIdentificacion;
        this.denominacion = denominacion;
        this.mailInstitucional = mailInstitucional;
        this.direccion = direccion;
        this.telefono = telefono;
        this.movil = movil;
        this.aceptacionMedios = aceptacionMedios;
        this.sim = sim;
        this.ssoId = ssoId;
        this.tipoUsuario = tipoUsuario;
        this.usuarioCreacion = usuarioCreacion;
        this.terminalCreacion = terminalCreacion;
        this.fechaCreacion = fechaCreacion;
        this.usuarioModificacion = usuarioModificacion;
        this.terminalModificacion = terminalModificacion;
        this.fechaModificacion = fechaModificacion;
        this.estado = estado;
    }

    @PrePersist
    private void prePersist() {
        if (estado == null || estado.isBlank()) {
            estado = "ACTIVO";
        }
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
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

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public AdminZonal getAdminZonal() {
        return adminZonal;
    }

    public void setAdminZonal(AdminZonal adminZonal) {
        this.adminZonal = adminZonal;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public String getTipoIdentificacion() {
        return tipoIdentificacion;
    }

    public void setTipoIdentificacion(String tipoIdentificacion) {
        this.tipoIdentificacion = tipoIdentificacion;
    }

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }

    public String getMailInstitucional() {
        return mailInstitucional;
    }

    public void setMailInstitucional(String mailInstitucional) {
        this.mailInstitucional = mailInstitucional;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getMovil() {
        return movil;
    }

    public void setMovil(String movil) {
        this.movil = movil;
    }

    public String getAceptacionMedios() {
        return aceptacionMedios;
    }

    public void setAceptacionMedios(String aceptacionMedios) {
        this.aceptacionMedios = aceptacionMedios;
    }

    public String getSim() {
        return sim;
    }

    public void setSim(String sim) {
        this.sim = sim;
    }

    public String getSsoId() {
        return ssoId;
    }

    public void setSsoId(String ssoId) {
        this.ssoId = ssoId;
    }

    public Long getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(Long tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public String getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public void setUsuarioCreacion(String usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
    }

    public String getTerminalCreacion() {
        return terminalCreacion;
    }

    public void setTerminalCreacion(String terminalCreacion) {
        this.terminalCreacion = terminalCreacion;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getUsuarioModificacion() {
        return usuarioModificacion;
    }

    public void setUsuarioModificacion(String usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
    }

    public String getTerminalModificacion() {
        return terminalModificacion;
    }

    public void setTerminalModificacion(String terminalModificacion) {
        this.terminalModificacion = terminalModificacion;
    }

    public Date getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(Date fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
