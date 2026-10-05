package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoDenunciante implements Comparable<DtoDenunciante> {

    private Long idDenunciante;

    private String nombre;

    private String apellido;

    private String cedula;

    private String tipoDenunciante;

    private String direccion;

    private String referencia;

    private String telefono;

    public DtoDenunciante() {
    }

    public DtoDenunciante(
            Long idDenunciante,
            String nombre,
            String apellido,
            String cedula,
            String tipoDenunciante,
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

    public String getTipoDenunciante() {
        return tipoDenunciante;
    }

    public void setTipoDenunciante(String tipoDenunciante) {
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

    @Override
    public int compareTo(DtoDenunciante o) {

        if (o == null) {
            return 1;
        }

        if (this.idDenunciante == null && o.idDenunciante == null) {
            return 0;
        }

        if (this.idDenunciante == null) {
            return -1;
        }

        if (o.idDenunciante == null) {
            return 1;
        }

        return this.idDenunciante.compareTo(o.idDenunciante);
    }

}