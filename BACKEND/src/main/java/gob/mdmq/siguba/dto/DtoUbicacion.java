package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import gob.mdmq.siguba.Entidades.AdminZonal;
import gob.mdmq.siguba.Entidades.Barrio;
import gob.mdmq.siguba.Entidades.Parroquia;
import gob.mdmq.siguba.Entidades.Predio;
import gob.mdmq.siguba.Entidades.UbicacionImagen;
import java.math.BigDecimal;
import java.util.List;
import lombok.ToString;

@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoUbicacion {

    private Long idUbicacion;
    private String direccion;
    private String referencia;
    private AdminZonal adminZonal;
    private Parroquia parroquia;
    private Barrio barrio;
    private Predio predio;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private List<UbicacionImagen> imagenes;

    public DtoUbicacion() {
    }

    public DtoUbicacion(Long idUbicacion,
                        String direccion,
                        String referencia,
                        AdminZonal adminZonal,
                        Parroquia parroquia,
                        Barrio barrio,
                        Predio predio,
                        BigDecimal latitud,
                        BigDecimal longitud,
                        List<UbicacionImagen> imagenes) {

        this.idUbicacion = idUbicacion;
        this.direccion = direccion;
        this.referencia = referencia;
        this.adminZonal = adminZonal;
        this.parroquia = parroquia;
        this.barrio = barrio;
        this.predio = predio;
        this.latitud = latitud;
        this.longitud = longitud;
        this.imagenes = imagenes;
    }

    public Long getIdUbicacion() {
        return idUbicacion;
    }

    public void setIdUbicacion(Long idUbicacion) {
        this.idUbicacion = idUbicacion;
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

    public AdminZonal getAdminZonal() {
        return adminZonal;
    }

    public void setAdminZonal(AdminZonal adminZonal) {
        this.adminZonal = adminZonal;
    }

    public Parroquia getParroquia() {
        return parroquia;
    }

    public void setParroquia(Parroquia parroquia) {
        this.parroquia = parroquia;
    }

    public Barrio getBarrio() {
        return barrio;
    }

    public void setBarrio(Barrio barrio) {
        this.barrio = barrio;
    }

    public Predio getPredio() {
        return predio;
    }

    public void setPredio(Predio predio) {
        this.predio = predio;
    }

    public BigDecimal getLatitud() {
        return latitud;
    }

    public void setLatitud(BigDecimal latitud) {
        this.latitud = latitud;
    }

    public BigDecimal getLongitud() {
        return longitud;
    }

    public void setLongitud(BigDecimal longitud) {
        this.longitud = longitud;
    }

    public List<UbicacionImagen> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<UbicacionImagen> imagenes) {
        this.imagenes = imagenes;
    }
}