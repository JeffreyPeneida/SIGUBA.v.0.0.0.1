package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoImagenInspeccion implements Comparable<DtoImagenInspeccion> {

    private Long idImagen;
    private Long idCodigoInterno;
    private String rutaImagen;

    public DtoImagenInspeccion() {
    }

    public DtoImagenInspeccion(
            Long idImagen,
            Long idCodigoInterno,
            String rutaImagen) {

        this.idImagen = idImagen;
        this.idCodigoInterno = idCodigoInterno;
        this.rutaImagen = rutaImagen;
    }

    public Long getIdImagen() {
        return idImagen;
    }

    public void setIdImagen(Long idImagen) {
        this.idImagen = idImagen;
    }

    public Long getIdCodigoInterno() {
        return idCodigoInterno;
    }

    public void setIdCodigoInterno(Long idCodigoInterno) {
        this.idCodigoInterno = idCodigoInterno;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public void setRutaImagen(String rutaImagen) {
        this.rutaImagen = rutaImagen;
    }

    @Override
    public int compareTo(DtoImagenInspeccion o) {

        if (o == null) {
            return 1;
        }

        if (this.idImagen == null && o.idImagen == null) {
            return 0;
        }

        if (this.idImagen == null) {
            return -1;
        }

        if (o.idImagen == null) {
            return 1;
        }

        return this.idImagen.compareTo(o.idImagen);
    }

}