package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoTecnico implements Comparable<DtoTecnico> {

    private Long idTecnico;
    private String nombre;
    private String apellido;
    private String cedula;
    private String tecnicoUsuario;
    private String password;
    private String correo;
    private String estado;

    public DtoTecnico() {
    }

    public DtoTecnico(Long idTecnico,
                      String nombre,
                      String apellido,
                      String cedula,
                      String tecnicoUsuario,
                      String password,
                      String correo,
                      String estado) {

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

    @Override
    public int compareTo(DtoTecnico o) {

        if (o == null) {
            return 1;
        }

        if (this.idTecnico == null && o.idTecnico == null) {
            return 0;
        }

        if (this.idTecnico == null) {
            return -1;
        }

        if (o.idTecnico == null) {
            return 1;
        }

        return this.idTecnico.compareTo(o.idTecnico);
    }
}