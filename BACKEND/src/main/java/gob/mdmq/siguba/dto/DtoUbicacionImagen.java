package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import gob.mdmq.siguba.Entidades.Ubicacion;
import lombok.ToString;

@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoUbicacionImagen {

    private Long idImagen;
    private Ubicacion ubicacion;
    private String rutaImagen;

    public DtoUbicacionImagen() {
    }

    public DtoUbicacionImagen(Long idImagen, Ubicacion ubicacion, String rutaImagen) {
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