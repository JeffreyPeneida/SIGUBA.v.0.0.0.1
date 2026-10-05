package gob.mdmq.siguba.Entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.ToString;

@Entity
@Table(name = "UBA_UBICACION", schema = "PROYECTO_UBA")
@ToString(exclude = {
    "adminZonal",
    "parroquia",
    "barrio",
    "predio",
    "imagenes"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Ubicacion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_UBICACION")
    private Long idUbicacion;

    @Column(name = "DIRECCION", nullable = false)
    private String direccion;

    @Column(name = "REFERENCIA")
    private String referencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ADMIN_ZONAL_ID")
    private AdminZonal adminZonal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARROQUIA_ID")
    private Parroquia parroquia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BARRIO_ID")
    private Barrio barrio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PREDIO_ID")
    private Predio predio;

    @Column(name = "LATITUD", precision = 10, scale = 8)
    private BigDecimal latitud;

    @Column(name = "LONGITUD", precision = 11, scale = 8)
    private BigDecimal longitud;

    @JsonIgnore
    @OneToMany(
        mappedBy = "ubicacion",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<UbicacionImagen> imagenes = new ArrayList<>();

    public Ubicacion() {
    }

    public Ubicacion(Long idUbicacion,
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

    // GETTERS Y SETTERS

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