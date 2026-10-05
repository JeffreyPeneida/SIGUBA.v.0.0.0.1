package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import gob.mdmq.siguba.Entidades.AdminZonal;
import gob.mdmq.siguba.Entidades.RolUsuario;
import java.util.Date;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoUsuario {

    private Integer idUsuario;
    private String nombre;
    private String apellido;
    private String cedula;

    @Temporal(TemporalType.DATE)
    private Date fechaNacimiento;

    private String usuario;
    private String mail;
    private AdminZonal adminZonal;
    private String password;
    private RolUsuario rol;
    private String tipoIdentificacion;
    private String denominacion;
    private String mailInstitucional;
    private String direccion;
    private String telefono;
    private String movil;
    private String aceptacionMedios;
    private String sim;
    private String ssoId;
    private Long tipoUsuario;
    private String usuarioCreacion;
    private String terminalCreacion;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    private String usuarioModificacion;
    private String terminalModificacion;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    private String estado;

    public DtoUsuario() {
    }

    public DtoUsuario(
            Integer idUsuario,
            String nombre,
            String apellido,
            String cedula,
            Date fechaNacimiento,
            String usuario,
            String mail,
            AdminZonal adminZonal,
            RolUsuario rol,
            String estado) {

        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.cedula = cedula;
        this.fechaNacimiento = fechaNacimiento;
        this.usuario = usuario;
        this.mail = mail;
        this.adminZonal = adminZonal;
        this.rol = rol;
        this.estado = estado;
    }

    public DtoUsuario(Integer idUsuario,
            String nombre,
            String apellido,
            String cedula,
            Date fechaNacimiento,
            String usuario,
            String mail,
            AdminZonal adminZonal,
            String password,
            RolUsuario rol,
            String tipoIdentificacion,
            String denominacion,
            String mailInstitucional,
            String direccion,
            String telefono,
            String movil,
            String aceptacionMedios,
            String sim,
            String ssoId,
            Long tipoUsuario,
            String usuarioCreacion,
            String terminalCreacion,
            Date fechaCreacion,
            String usuarioModificacion,
            String terminalModificacion,
            Date fechaModificacion,
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
