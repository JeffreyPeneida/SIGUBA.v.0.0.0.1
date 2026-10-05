package gob.mdmq.siguba.Entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import jakarta.persistence.*;
import lombok.ToString;

@Entity
@Table(name = "UBA_IMAGEN_DENUNCIA", schema = "PROYECTO_UBA")
@ToString(exclude = "tramite")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ImagenDenuncia implements Serializable {

    private static final long serialVersionUID = 1L;

    public ImagenDenuncia() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_IMAGEN")
    private Long idImagen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "ID_CODIGO_INTERNO"
    )
    private Tramite tramite;

    @Lob
    @Column(name = "RUTA_IMAGEN")
    private String rutaImagen;

    public ImagenDenuncia(
            Long idImagen,
            Tramite tramite,
            String rutaImagen) {

        this.idImagen = idImagen;
        this.tramite = tramite;
        this.rutaImagen = rutaImagen;
    }

    public Long getIdImagen() {
        return idImagen;
    }

    public void setIdImagen(Long idImagen) {
        this.idImagen = idImagen;
    }

    public Tramite getTramite() {
        return tramite;
    }

    public void setTramite(Tramite tramite) {
        this.tramite = tramite;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public void setRutaImagen(String rutaImagen) {
        this.rutaImagen = rutaImagen;
    }
}