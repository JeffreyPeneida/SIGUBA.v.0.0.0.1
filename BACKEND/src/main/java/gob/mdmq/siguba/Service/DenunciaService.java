package gob.mdmq.siguba.Service;

import gob.mdmq.siguba.Entidades.*;
import gob.mdmq.siguba.Exception.ReglaNegocioException;
import gob.mdmq.siguba.Repository.*;
import gob.mdmq.siguba.dto.DtoNuevaDenuncia;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Alta de una denuncia ciudadana.
 *
 * Crea en una sola transaccion el denunciante, la ubicacion y el tramite, y
 * sube las imagenes a MinIO guardando solo su ruta. Si algo falla, no queda un
 * denunciante suelto sin tramite.
 */
@Service
public class DenunciaService {

    private static final Logger log = LoggerFactory.getLogger(DenunciaService.class);

    private final DenuncianteRepository denuncianteRepository;
    private final UbicacionRepository ubicacionRepository;
    private final TramiteRepository tramiteRepository;
    private final TipoDenuncianteRepository tipoDenuncianteRepository;
    private final DependenciaRepository dependenciaRepository;
    private final AdminZonalRepository adminZonalRepository;
    private final ParroquiaRepository parroquiaRepository;
    private final BarrioRepository barrioRepository;
    private final PredioRepository predioRepository;
    private final EspecieRepository especieRepository;
    private final ImagenDenunciaRepository imagenDenunciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlmacenamientoService almacenamiento;

    public DenunciaService(
            DenuncianteRepository denuncianteRepository,
            UbicacionRepository ubicacionRepository,
            TramiteRepository tramiteRepository,
            TipoDenuncianteRepository tipoDenuncianteRepository,
            DependenciaRepository dependenciaRepository,
            AdminZonalRepository adminZonalRepository,
            ParroquiaRepository parroquiaRepository,
            BarrioRepository barrioRepository,
            PredioRepository predioRepository,
            EspecieRepository especieRepository,
            ImagenDenunciaRepository imagenDenunciaRepository,
            UsuarioRepository usuarioRepository,
            AlmacenamientoService almacenamiento) {

        this.denuncianteRepository = denuncianteRepository;
        this.ubicacionRepository = ubicacionRepository;
        this.tramiteRepository = tramiteRepository;
        this.tipoDenuncianteRepository = tipoDenuncianteRepository;
        this.dependenciaRepository = dependenciaRepository;
        this.adminZonalRepository = adminZonalRepository;
        this.parroquiaRepository = parroquiaRepository;
        this.barrioRepository = barrioRepository;
        this.predioRepository = predioRepository;
        this.especieRepository = especieRepository;
        this.imagenDenunciaRepository = imagenDenunciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.almacenamiento = almacenamiento;
    }

    /** Codigo que tendra la proxima denuncia, para mostrarlo antes de guardar. */
    public String siguienteCodigo() {
        Long ultimo = tramiteRepository.obtenerUltimoId();
        return "UBA-%03d".formatted((ultimo == null ? 0 : ultimo) + 1);
    }

    @Transactional
    public Tramite registrar(DtoNuevaDenuncia datos, List<MultipartFile> imagenes, String registradoPor) {

        if (datos == null) {
            throw new ReglaNegocioException("No se recibieron los datos de la denuncia");
        }

        if (datos.getNarracion() == null || datos.getNarracion().isBlank()) {
            throw new ReglaNegocioException("La narración de los hechos es obligatoria");
        }

        if (datos.getDireccion() == null || datos.getDireccion().isBlank()) {
            throw new ReglaNegocioException("La dirección del lugar es obligatoria");
        }

        // --- denunciante -------------------------------------------------
        Denunciante denunciante = new Denunciante();
        denunciante.setNombre(textoODefecto(datos.getNombre(), "SIN NOMBRE"));
        denunciante.setApellido(textoODefecto(datos.getApellido(), "SIN APELLIDO"));
        denunciante.setCedula(textoODefecto(datos.getCedula(), "0000000000"));
        denunciante.setDireccion(datos.getDireccion());
        denunciante.setReferencia(datos.getReferencia());
        denunciante.setTelefono(datos.getTelefono());

        if (datos.getTipoDenunciante() != null) {
            tipoDenuncianteRepository.findById(datos.getTipoDenunciante())
                    .ifPresent(denunciante::setTipoDenunciante);
        }

        denunciante = denuncianteRepository.save(denunciante);

        // --- ubicacion ---------------------------------------------------
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setDireccion(datos.getDireccion());
        ubicacion.setReferencia(datos.getReferencia());
        ubicacion.setLatitud(datos.getLatitud());
        ubicacion.setLongitud(datos.getLongitud());

        if (datos.getAdminZonal() != null) {
            adminZonalRepository.findById(datos.getAdminZonal()).ifPresent(ubicacion::setAdminZonal);
        }
        if (datos.getParroquia() != null) {
            parroquiaRepository.findById(datos.getParroquia()).ifPresent(ubicacion::setParroquia);
        }
        if (datos.getBarrio() != null) {
            barrioRepository.findById(datos.getBarrio()).ifPresent(ubicacion::setBarrio);
        }
        if (datos.getPredio() != null) {
            predioRepository.findById(datos.getPredio()).ifPresent(ubicacion::setPredio);
        }

        ubicacion = ubicacionRepository.save(ubicacion);

        // --- tramite -----------------------------------------------------
        Tramite tramite = new Tramite();
        tramite.setIdTramite(siguienteCodigo());
        tramite.setFechaDenuncia(new Date());
        tramite.setNarracion(datos.getNarracion());
        tramite.setLatitud(datos.getLatitud());
        tramite.setLongitud(datos.getLongitud());
        tramite.setDenunciante(denunciante);
        tramite.setUbicacion(ubicacion);
        tramite.setEstado("ACTIVO");

        // Quien la registro, tomado de la sesion. Sin esto "Mis denuncias" no
        // tenia forma de saber de quien es cada una.
        if (registradoPor != null) {
            usuarioRepository.buscarPorUsuario(registradoPor).ifPresent(tramite::setUsuario);
        }

        if (datos.getDependencia() != null) {
            dependenciaRepository.findById(datos.getDependencia()).ifPresent(tramite::setDependencia);
        }
        if (datos.getBarrio() != null) {
            barrioRepository.findById(datos.getBarrio()).ifPresent(tramite::setBarrio);
        }

        // Las especies denunciadas son lo que define la plaga a tratar. El
        // formulario ya las enviaba, pero no se guardaban: el tecnico recibia
        // el tramite sin saber de que plaga se trataba.
        if (datos.getEspecies() != null && !datos.getEspecies().isEmpty()) {
            tramite.setEspecies(especieRepository.findAllById(datos.getEspecies()));
        }

        tramite = tramiteRepository.save(tramite);

        // --- imagenes ----------------------------------------------------
        // Van a MinIO; en la base solo queda la ruta. Que falle una imagen no
        // debe tumbar la denuncia, que es lo que el ciudadano vino a registrar.
        if (imagenes != null) {
            for (MultipartFile imagen : imagenes) {
                if (imagen == null || imagen.isEmpty()) continue;
                try {
                    String ruta = almacenamiento.guardar(imagen, "denuncias");

                    // Sin esta fila la foto queda en MinIO pero el tramite no
                    // sabe que existe: el tecnico no veia ninguna evidencia.
                    ImagenDenuncia fila = new ImagenDenuncia();
                    fila.setTramite(tramite);
                    fila.setRutaImagen(ruta);
                    imagenDenunciaRepository.save(fila);

                } catch (RuntimeException e) {
                    log.warn("No se pudo guardar una imagen de la denuncia {}",
                            tramite.getIdTramite(), e);
                }
            }
        }

        return tramite;
    }

    private static String textoODefecto(String valor, String porDefecto) {
        return (valor == null || valor.isBlank()) ? porDefecto : valor.trim();
    }
}
