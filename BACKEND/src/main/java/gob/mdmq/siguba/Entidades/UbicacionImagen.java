package gob.mdmq.siguba.Entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import jakarta.persistence.*;
import lombok.ToString;

@Entity
@Table(name = "UBA_UBICACION_IMAGEN", schema = "PROYECTO_UBA")
@ToString(exclude = "ubicacion")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class UbicacionImagen implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_IMAGEN")
    private Long idImagen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UBICACION")
    private Ubicacion ubicacion;

    @Column(name = "RUTA_IMAGEN")
    private String rutaImagen;

    public UbicacionImagen() {
    }

    public UbicacionImagen(Long idImagen, Ubicacion ubicacion, String rutaImagen) {
        this.idImagen = idImagen;
        this.ubicacion = ubicacion;
        this.rutaImagen = rutaImagen;
    }

    public Long getIdImagen() {
        return idImagen;
    }

    public void setIdImagen(Long idImagen) {
        this.idImagen = idImagen;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Ubicacion ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public void setRutaImagen(String rutaImagen) {
        this.rutaImagen = rutaImagen;
    }
}