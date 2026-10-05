package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

/**
 * Denuncia ciudadana tal y como la envia el formulario.
 *
 * Los alias existen porque el formulario manda los nombres con guion bajo
 * (admin_zonal, tipo_denunciante); se aceptan ambas formas para no romper el
 * frontend ni obligar a nombres feos en Java.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DtoNuevaDenuncia {

    // --- denunciante ---
    private String nombre;
    private String apellido;
    private String cedula;
    private String telefono;

    @JsonAlias({"tipo_denunciante", "tipoDenunciante"})
    private Long tipoDenunciante;

    private Long dependencia;

    // --- ubicacion ---
    private String direccion;
    private String referencia;

    @JsonAlias({"admin_zonal", "adminZonal"})
    private Long adminZonal;

    private Long parroquia;
    private Long barrio;
    private Long predio;

    private BigDecimal latitud;
    private BigDecimal longitud;

    // --- denuncia ---
    private String narracion;
    private List<Long> especies;

    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }

    public String getApellido() { return apellido; }
    public void setApellido(String v) { this.apellido = v; }

    public String getCedula() { return cedula; }
    public void setCedula(String v) { this.cedula = v; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String v) { this.telefono = v; }

    public Long getTipoDenunciante() { return tipoDenunciante; }
    public void setTipoDenunciante(Long v) { this.tipoDenunciante = v; }

    public Long getDependencia() { return dependencia; }
    public void setDependencia(Long v) { this.dependencia = v; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String v) { this.direccion = v; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String v) { this.referencia = v; }

    public Long getAdminZonal() { return adminZonal; }
    public void setAdminZonal(Long v) { this.adminZonal = v; }

    public Long getParroquia() { return parroquia; }
    public void setParroquia(Long v) { this.parroquia = v; }

    public Long getBarrio() { return barrio; }
    public void setBarrio(Long v) { this.barrio = v; }

    public Long getPredio() { return predio; }
    public void setPredio(Long v) { this.predio = v; }

    public BigDecimal getLatitud() { return latitud; }
    public void setLatitud(BigDecimal v) { this.latitud = v; }

    public BigDecimal getLongitud() { return longitud; }
    public void setLongitud(BigDecimal v) { this.longitud = v; }

    public String getNarracion() { return narracion; }
    public void setNarracion(String v) { this.narracion = v; }

    public List<Long> getEspecies() { return especies; }
    public void setEspecies(List<Long> v) { this.especies = v; }
}
