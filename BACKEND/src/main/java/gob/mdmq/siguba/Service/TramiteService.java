package gob.mdmq.siguba.Service;

import gob.mdmq.siguba.Entidades.Tramite;
import gob.mdmq.siguba.dto.DtoTramite;
import gob.mdmq.siguba.Repository.TramiteRepository;

import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TramiteService {

    @Autowired
    private TramiteRepository tramiteRepository;

    @Transactional
    public boolean guardarTramite(DtoTramite dtoTramite) {

        boolean respuesta = false;

        try {

            Tramite tramite = new Tramite();

            tramite.setIdCodigoInterno(dtoTramite.getIdCodigoInterno());
            tramite.setIdTramite(dtoTramite.getIdTramite());

            if (dtoTramite.getFechaDenuncia() == null) {
                tramite.setFechaDenuncia(new Date());
            } else {
                tramite.setFechaDenuncia(dtoTramite.getFechaDenuncia());
            }

            tramite.setNarracion(dtoTramite.getNarracion());
            tramite.setLatitud(dtoTramite.getLatitud());
            tramite.setLongitud(dtoTramite.getLongitud());

            tramite.setFechaInspeccion(dtoTramite.getFechaInspeccion());
            tramite.setHoraInspeccion(dtoTramite.getHoraInspeccion());
            tramite.setFechaAsignacionTecnico(dtoTramite.getFechaAsignacionTecnico());

            tramite.setDenunciante(dtoTramite.getDenunciante());
            tramite.setTecnico(dtoTramite.getTecnico());
            tramite.setUbicacion(dtoTramite.getUbicacion());
            tramite.setDependencia(dtoTramite.getDependencia());
            tramite.setAreaInspeccion(dtoTramite.getAreaInspeccion());
            tramite.setMotivoInspeccion(dtoTramite.getMotivoInspeccion());
            tramite.setTipoEvidencia(dtoTramite.getTipoEvidencia());
            tramite.setUsuario(dtoTramite.getUsuario());
            tramite.setBarrio(dtoTramite.getBarrio());

            tramite.setEspecies(dtoTramite.getEspecies());

            tramite.setDescripcion(dtoTramite.getDescripcion());
            tramite.setConclusiones(dtoTramite.getConclusiones());
            tramite.setRecomendaciones(dtoTramite.getRecomendaciones());

            if (dtoTramite.getEstado() == null) {
                tramite.setEstado("ACTIVO");
            } else {
                tramite.setEstado(dtoTramite.getEstado());
            }

            tramiteRepository.save(tramite);

            respuesta = true;

        } catch (Exception e) {
            e.printStackTrace();
            respuesta = false;
        }

        return respuesta;
    }

    public List<Tramite> obtenerTramites() {

        try {
            return tramiteRepository.obtenerTramites();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Tramite> obtenerDenuncias() {

        try {
            return tramiteRepository.obtenerDenuncias();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Tramite> obtenerInspecciones() {

        try {
            return tramiteRepository.obtenerInspecciones();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Optional<Tramite> buscarPorCodigo(String codigo) {

        try {
            return tramiteRepository.buscarPorCodigo(codigo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public Optional<Tramite> buscarPorIdInterno(Long idInterno) {

        try {
            return tramiteRepository.findByIdCodigoInterno(idInterno);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public List<Tramite> buscarPorCedula(String cedula) {

        try {
            return tramiteRepository.buscarPorCedula(cedula);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Tramite> buscarDeUsuario(Integer idUsuario, String cedula) {
        return tramiteRepository.buscarDeUsuario(idUsuario, cedula);
    }

    public List<Tramite> obtenerPorTecnico(Long idTecnico) {

        try {
            return tramiteRepository.obtenerPorTecnico(idTecnico);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Tramite> obtenerSinTecnico() {

        try {
            return tramiteRepository.obtenerSinTecnico();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Transactional
    public boolean actualizarUbicacion(
            Long idInterno,
            java.math.BigDecimal latitud,
            java.math.BigDecimal longitud) {

        try {

            return tramiteRepository.actualizarUbicacion(
                    idInterno,
                    latitud,
                    longitud) > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Transactional
    public boolean actualizarCodigo(
            Long idInterno,
            String codigo) {

        try {

            return tramiteRepository.actualizarCodigo(
                    idInterno,
                    codigo) > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Transactional
    public boolean eliminar(Long idInterno) {

        try {

            return tramiteRepository.eliminar(idInterno) > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Transactional
    public boolean restaurar(Long idInterno) {

        try {

            return tramiteRepository.restaurar(idInterno) > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public Long obtenerUltimoId() {

        try {
            return tramiteRepository.obtenerUltimoId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0L;
    }

    public boolean existeCodigo(String codigo) {

        try {
            return tramiteRepository.existsByIdTramite(codigo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean existeIdInterno(Long idInterno) {

        try {
            return tramiteRepository.existsByIdCodigoInterno(idInterno);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ==================================================
    // OBTENER TÉCNICO ASIGNADO
    // ==================================================
    public Long obtenerTecnicoAsignado(Long idCodigoInterno) {

        try {
            return tramiteRepository.obtenerTecnicoAsignado(
                    idCodigoInterno);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ==================================================
    // OBTENER CÓDIGO TRÁMITE
    // ==================================================
    public String obtenerCodigoTramite(Long idCodigoInterno) {

        try {
            return tramiteRepository.obtenerCodigoTramite(
                    idCodigoInterno);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ==================================================
    // ACTUALIZAR INSPECCIÓN
    // ==================================================
    @Transactional
    public boolean actualizarInspeccion(
            Date fechaInspeccion,
            String horaInspeccion,
            Long idArea,
            Long idMotivo,
            String descripcion,
            String conclusiones,
            String recomendaciones,
            Long idRegistra,
            Long idCodigoInterno) {

        try {

            // El formulario envia la hora como texto ("09:30"), pero
            // Tramite.horaInspeccion esta mapeado como TemporalType.TIME.
            // Sin esta conversion Hibernate rechazaba el UPDATE completo.
            Time hora = null;

            if (horaInspeccion != null && !horaInspeccion.isBlank()) {

                String h = horaInspeccion.trim();

                if (h.length() == 5) {
                    h = h + ":00";
                }

                hora = Time.valueOf(h);
            }

            return tramiteRepository.actualizarInspeccion(
                    fechaInspeccion,
                    hora,
                    idArea,
                    idMotivo,
                    descripcion,
                    conclusiones,
                    recomendaciones,
                    idRegistra,
                    idCodigoInterno) > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ==================================================
    // ASIGNAR TÉCNICO
    // ==================================================
    @Transactional
    public boolean asignarTecnico(
            Long idTecnico,
            Long idInterno) {

        try {

            return tramiteRepository.asignarTecnico(
                    idTecnico,
                    idInterno) > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
