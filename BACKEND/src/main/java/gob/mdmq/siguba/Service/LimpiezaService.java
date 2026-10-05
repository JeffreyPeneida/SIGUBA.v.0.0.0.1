package gob.mdmq.siguba.Service;

import gob.mdmq.siguba.Entidades.ImagenDenuncia;
import gob.mdmq.siguba.Entidades.Tramite;
import gob.mdmq.siguba.Exception.RecursoNoEncontradoException;
import gob.mdmq.siguba.Repository.ImagenDenunciaRepository;
import gob.mdmq.siguba.Repository.NivelInspeccionRepository;
import gob.mdmq.siguba.Repository.TramiteRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Borrado definitivo de tramites, para depurar datos de prueba.
 *
 * Es distinto de "anular": anular deja el tramite en la base con estado
 * INACTIVO y se puede deshacer; esto lo borra y no hay vuelta atras. Por eso el
 * endpoint que lo expone esta restringido a la administracion.
 */
@Service
public class LimpiezaService {

    private static final Logger log = LoggerFactory.getLogger(LimpiezaService.class);

    private final TramiteRepository tramiteRepository;
    private final ImagenDenunciaRepository imagenDenunciaRepository;
    private final NivelInspeccionRepository nivelInspeccionRepository;
    private final AlmacenamientoService almacenamiento;

    public LimpiezaService(TramiteRepository tramiteRepository,
                           ImagenDenunciaRepository imagenDenunciaRepository,
                           NivelInspeccionRepository nivelInspeccionRepository,
                           AlmacenamientoService almacenamiento) {
        this.tramiteRepository = tramiteRepository;
        this.imagenDenunciaRepository = imagenDenunciaRepository;
        this.nivelInspeccionRepository = nivelInspeccionRepository;
        this.almacenamiento = almacenamiento;
    }

    @Transactional
    public void borrarTramite(Long idCodigoInterno) {

        Tramite tramite = tramiteRepository.findById(idCodigoInterno)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el trámite " + idCodigoInterno));

        // Las fotos viven en MinIO: borrar solo la fila dejaria los archivos
        // huerfanos ocupando espacio para siempre.
        for (ImagenDenuncia imagen : imagenDenunciaRepository.obtenerPorTramite(idCodigoInterno)) {
            if (imagen.getRutaImagen() != null && !imagen.getRutaImagen().isBlank()) {
                almacenamiento.eliminar(imagen.getRutaImagen());
            }
        }

        // Su clave foranea es NO_ACTION y bloquearia el borrado; el resto de
        // tablas hijas estan en CASCADE y se van solas.
        nivelInspeccionRepository.borrarPorTramite(idCodigoInterno);

        tramiteRepository.delete(tramite);

        log.warn("Trámite {} ({}) borrado definitivamente.",
                idCodigoInterno, tramite.getIdTramite());
    }
}
