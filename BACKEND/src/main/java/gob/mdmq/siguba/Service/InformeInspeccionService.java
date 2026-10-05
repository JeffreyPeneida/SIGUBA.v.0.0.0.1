package gob.mdmq.siguba.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import gob.mdmq.siguba.Entidades.*;
import gob.mdmq.siguba.Exception.RecursoNoEncontradoException;
import gob.mdmq.siguba.Exception.ReglaNegocioException;
import gob.mdmq.siguba.Repository.ImagenInspeccionRepository;
import gob.mdmq.siguba.Repository.InformeInspeccionRepository;
import gob.mdmq.siguba.Repository.TramiteRepository;
import gob.mdmq.siguba.dto.DtoInformeInspeccion;
import gob.mdmq.siguba.dto.DtoInformeInspeccion.DetalleEspecie;
import gob.mdmq.siguba.dto.DtoInformeInspeccion.Foto;
import gob.mdmq.siguba.dto.DtoInformeInspeccion.GrupoRecomendacion;
import gob.mdmq.siguba.dto.DtoInformeInspeccion.Participante;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Informe de inspeccion: borrador, guardado y datos para exportarlo.
 *
 * Mientras el tramite no tenga informe se devuelve un borrador armado con lo
 * que ya se sabe (lugar, especies, tecnico, textos de la inspeccion). Al
 * guardarlo, esos datos quedan copiados en el informe y se editan alli.
 */
@Service
public class InformeInspeccionService {

    private static final Logger log = LoggerFactory.getLogger(InformeInspeccionService.class);

    /** Entidades a las que el modelo oficial dirige recomendaciones. */
    private static final List<String> ENTIDADES_RECOMENDACION = List.of(
            "Administración Zonal", "EMASEO EP", "EPMMOP", "EPMAPS",
            "Ciudadanía", "Comerciantes");

    private static final String INSTITUCION = "UBA - MDMQ";

    /** Mismo tope que la pantalla; el anexo no pasa de unas pocas paginas. */
    private static final int MAX_FOTOS = 20;

    private static final TypeReference<List<Participante>> T_PARTICIPANTES = new TypeReference<>() {};
    private static final TypeReference<List<DetalleEspecie>> T_DETALLE = new TypeReference<>() {};
    private static final TypeReference<List<GrupoRecomendacion>> T_RECOMENDACIONES = new TypeReference<>() {};

    private final InformeInspeccionRepository informes;
    private final TramiteRepository tramites;
    private final ImagenInspeccionRepository imagenes;
    private final AlmacenamientoService almacenamiento;
    private final ObjectMapper json;

    public InformeInspeccionService(InformeInspeccionRepository informes,
                                    TramiteRepository tramites,
                                    ImagenInspeccionRepository imagenes,
                                    AlmacenamientoService almacenamiento,
                                    ObjectMapper json) {
        this.informes = informes;
        this.tramites = tramites;
        this.imagenes = imagenes;
        this.almacenamiento = almacenamiento;
        this.json = json;
    }

    /** Informe guardado o, si aun no existe, el borrador del tramite. */
    @Transactional(readOnly = true)
    public DtoInformeInspeccion obtener(Long idCodigoInterno) {

        Tramite tramite = buscarTramite(idCodigoInterno);

        DtoInformeInspeccion dto = informes.findById(idCodigoInterno)
                .map(this::aDto)
                .orElseGet(() -> borrador(tramite));

        dto.setIdCodigoInterno(idCodigoInterno);
        dto.setCodigoTramite(tramite.getIdTramite());
        dto.setFotos(fotos(idCodigoInterno));

        return dto;
    }

    @Transactional
    public DtoInformeInspeccion guardar(Long idCodigoInterno, DtoInformeInspeccion dto,
                                        String usuario) {

        Tramite tramite = buscarTramite(idCodigoInterno);
        validar(dto);

        InformeInspeccion i = informes.findById(idCodigoInterno)
                .orElseGet(InformeInspeccion::new);

        i.setIdCodigoInterno(idCodigoInterno);
        i.setDocumentoAtendido(texto(dto.getDocumentoAtendido()));
        i.setTipoInspeccion(texto(dto.getTipoInspeccion()));
        i.setFechaInspeccion(dto.getFechaInspeccion());
        i.setPlagas(texto(dto.getPlagas()));
        i.setNivel(texto(dto.getNivel()));
        i.setAsunto(texto(dto.getAsunto()));
        i.setDireccion(texto(dto.getDireccion()));
        i.setSectorBarrio(texto(dto.getSectorBarrio()));
        i.setAdminZonal(texto(dto.getAdminZonal()));
        i.setTipoLugar(texto(dto.getTipoLugar()));
        i.setAreasSupervisadas(texto(dto.getAreasSupervisadas()));
        i.setCoordenadas(texto(dto.getCoordenadas()));
        i.setBeneficiarios(texto(dto.getBeneficiarios()));
        i.setPersonaContactada(texto(dto.getPersonaContactada()));
        i.setFactoresRiesgo(texto(dto.getFactoresRiesgo()));
        i.setDiagnostico(texto(dto.getDiagnostico()));
        i.setTipoControl(texto(dto.getTipoControl()));
        i.setProgramaActuacion(texto(dto.getProgramaActuacion()));
        i.setConclusiones(texto(dto.getConclusiones()));
        i.setElaboradoNombre(texto(dto.getElaboradoNombre()));
        i.setElaboradoCargo(texto(dto.getElaboradoCargo()));
        i.setRevisadoNombre(texto(dto.getRevisadoNombre()));
        i.setRevisadoCargo(texto(dto.getRevisadoCargo()));
        i.setFechaElaboracion(dto.getFechaElaboracion());

        // Filas vacias que la pantalla deja al agregar y no rellenar: fuera.
        i.setParticipantes(aJson(dto.getParticipantes().stream()
                .filter(p -> hay(p.getNombre()) || hay(p.getCargo()))
                .toList()));
        i.setDetalleEspecies(aJson(dto.getDetalleEspecies().stream()
                .filter(d -> hay(d.getEspecie()))
                .toList()));
        i.setRecomendaciones(aJson(dto.getRecomendaciones().stream()
                .filter(g -> hay(g.getEntidad()))
                .map(g -> new GrupoRecomendacion(g.getEntidad().trim(),
                        g.getItems().stream().filter(this::hay).map(String::trim).toList()))
                .toList()));

        i.setFechaActualizacion(LocalDateTime.now());
        i.setActualizadoPor(usuario);

        informes.save(i);
        guardarFotos(tramite, dto.getFotos());

        return obtener(idCodigoInterno);
    }

    /** Informe listo para exportar, con el contenido de cada foto ya descargado. */
    @Transactional(readOnly = true)
    public InformeExportable exportable(Long idCodigoInterno) {

        DtoInformeInspeccion informe = obtener(idCodigoInterno);
        List<byte[]> contenido = new ArrayList<>();

        for (Foto f : informe.getFotos()) {
            contenido.add(leer(f.getRuta()));
        }

        return new InformeExportable(informe, contenido);
    }

    /**
     * Informe con el contenido de sus fotos; null en la posicion de una foto
     * que no se pudo leer, para no perder la numeracion del anexo.
     */
    public record InformeExportable(DtoInformeInspeccion informe, List<byte[]> fotos) {
    }

    // ------------------------------------------------------------------ borrador

    private DtoInformeInspeccion borrador(Tramite t) {

        DtoInformeInspeccion d = new DtoInformeInspeccion();

        String especies = t.getEspecies() == null ? null : t.getEspecies().stream()
                .map(Especie::getNombre)
                .collect(Collectors.joining(", "));

        d.setDocumentoAtendido(t.getIdTramite() != null ? "Denuncia N° " + t.getIdTramite() : null);
        d.setTipoInspeccion(t.getMotivoInspeccion() != null ? t.getMotivoInspeccion().getNombre() : null);
        d.setFechaInspeccion(fechaHora(t.getFechaInspeccion(), t.getHoraInspeccion()));
        d.setPlagas(especies);
        d.setAsunto("Plan de Manejo Integrado de Plagas");

        Ubicacion u = t.getUbicacion();
        d.setDireccion(u != null ? u.getDireccion() : null);
        d.setSectorBarrio(t.getBarrio() != null ? t.getBarrio().getNombre()
                : u != null && u.getBarrio() != null ? u.getBarrio().getNombre() : null);
        d.setAdminZonal(u != null && u.getAdminZonal() != null ? u.getAdminZonal().getNombre() : null);
        d.setTipoLugar(t.getAreaInspeccion() != null ? t.getAreaInspeccion().getNombre() : null);
        d.setCoordenadas(coordenadas(t));

        Tecnico tec = t.getTecnico();
        String tecnico = tec != null ? (tec.getNombre() + " " + tec.getApellido()).trim() : null;
        if (hay(tecnico)) {
            d.getParticipantes().add(new Participante(tecnico, null, INSTITUCION));
            d.setElaboradoNombre(tecnico);
        }

        if (t.getEspecies() != null) {
            t.getEspecies().forEach(e -> d.getDetalleEspecies()
                    .add(new DetalleEspecie(e.getNombre(), null, null)));
        }

        d.setDiagnostico(t.getDescripcion());
        d.setConclusiones(t.getConclusiones());

        // Lo que se escribio como recomendacion general en la inspeccion va en
        // la primera entidad; el tecnico lo reparte al editar.
        ENTIDADES_RECOMENDACION.forEach(e -> d.getRecomendaciones()
                .add(new GrupoRecomendacion(e, new ArrayList<>())));
        if (hay(t.getRecomendaciones())) {
            d.getRecomendaciones().get(0).getItems().add(t.getRecomendaciones().trim());
        }

        d.setFechaElaboracion(LocalDate.now());
        return d;
    }

    private DtoInformeInspeccion aDto(InformeInspeccion i) {

        DtoInformeInspeccion d = new DtoInformeInspeccion();

        d.setGuardado(true);
        d.setFechaActualizacion(i.getFechaActualizacion());
        d.setActualizadoPor(i.getActualizadoPor());
        d.setDocumentoAtendido(i.getDocumentoAtendido());
        d.setTipoInspeccion(i.getTipoInspeccion());
        d.setFechaInspeccion(i.getFechaInspeccion());
        d.setPlagas(i.getPlagas());
        d.setNivel(i.getNivel());
        d.setAsunto(i.getAsunto());
        d.setDireccion(i.getDireccion());
        d.setSectorBarrio(i.getSectorBarrio());
        d.setAdminZonal(i.getAdminZonal());
        d.setTipoLugar(i.getTipoLugar());
        d.setAreasSupervisadas(i.getAreasSupervisadas());
        d.setCoordenadas(i.getCoordenadas());
        d.setBeneficiarios(i.getBeneficiarios());
        d.setPersonaContactada(i.getPersonaContactada());
        d.setParticipantes(deJson(i.getParticipantes(), T_PARTICIPANTES));
        d.setDetalleEspecies(deJson(i.getDetalleEspecies(), T_DETALLE));
        d.setFactoresRiesgo(i.getFactoresRiesgo());
        d.setDiagnostico(i.getDiagnostico());
        d.setTipoControl(i.getTipoControl());
        d.setProgramaActuacion(i.getProgramaActuacion());
        d.setConclusiones(i.getConclusiones());
        d.setRecomendaciones(deJson(i.getRecomendaciones(), T_RECOMENDACIONES));
        d.setElaboradoNombre(i.getElaboradoNombre());
        d.setElaboradoCargo(i.getElaboradoCargo());
        d.setRevisadoNombre(i.getRevisadoNombre());
        d.setRevisadoCargo(i.getRevisadoCargo());
        d.setFechaElaboracion(i.getFechaElaboracion());
        return d;
    }

    // ------------------------------------------------------------------ fotos

    private List<Foto> fotos(Long idCodigoInterno) {
        return imagenes.obtenerPorTramite(idCodigoInterno).stream()
                .map(img -> new Foto(img.getRutaImagen(),
                        almacenamiento.urlDe(img.getRutaImagen()),
                        img.getDescripcion()))
                .toList();
    }

    /**
     * Las fotos del informe son las de la inspeccion: se reemplaza la lista
     * entera para respetar el orden y los pies de foto que llegan. Los
     * archivos en MinIO no se borran; solo deja de apuntarlos el tramite.
     */
    private void guardarFotos(Tramite tramite, List<Foto> fotos) {

        imagenes.borrarPorTramite(tramite.getIdCodigoInterno());
        imagenes.flush();

        int orden = 1;
        for (Foto f : fotos) {
            ImagenInspeccion img = new ImagenInspeccion();
            img.setTramite(tramite);
            img.setRutaImagen(f.getRuta());
            img.setDescripcion(texto(f.getDescripcion()));
            img.setOrden(orden++);
            imagenes.save(img);
        }
    }

    private byte[] leer(String ruta) {
        try (InputStream in = almacenamiento.leer(ruta)) {
            return in.readAllBytes();
        } catch (Exception e) {
            // Una foto perdida no debe impedir entregar el informe.
            log.warn("No se pudo leer la foto {} del informe", ruta, e);
            return null;
        }
    }

    // ------------------------------------------------------------------ utilidades

    private void validar(DtoInformeInspeccion dto) {

        String control = texto(dto.getTipoControl());
        if (control != null && !control.equals("ACTIVO") && !control.equals("PASIVO")) {
            throw new ReglaNegocioException("El tipo de control debe ser ACTIVO o PASIVO");
        }

        if (dto.getFotos().size() > MAX_FOTOS) {
            throw new ReglaNegocioException(
                    "El anexo fotográfico admite como máximo " + MAX_FOTOS + " fotos");
        }

        // Solo fotos subidas como fotos de inspeccion: evita colgar en el
        // informe cualquier archivo del bucket (p. ej. fotos de otra denuncia).
        for (Foto f : dto.getFotos()) {
            if (!hay(f.getRuta()) || !f.getRuta().startsWith("inspecciones/")) {
                throw new ReglaNegocioException("Foto no válida: " + f.getRuta());
            }
        }
    }

    private Tramite buscarTramite(Long id) {
        return tramites.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el trámite " + id));
    }

    private static String coordenadas(Tramite t) {
        if (t.getLatitud() == null || t.getLongitud() == null) return null;
        return String.format(Locale.ROOT, "%.6f, %.6f",
                t.getLatitud().doubleValue(), t.getLongitud().doubleValue());
    }

    /**
     * Une las columnas DATE y TIME del tramite. El driver las entrega como
     * java.sql.Date/Time en la zona de la JVM; toInstant() no sirve en ellas.
     */
    private static LocalDateTime fechaHora(Date fecha, Date hora) {
        if (fecha == null) return null;
        ZoneId zona = ZoneId.systemDefault();
        LocalDate dia = Instant.ofEpochMilli(fecha.getTime()).atZone(zona).toLocalDate();
        LocalTime hh = hora != null
                ? Instant.ofEpochMilli(hora.getTime()).atZone(zona).toLocalTime().withSecond(0).withNano(0)
                : LocalTime.MIDNIGHT;
        return dia.atTime(hh);
    }

    private String aJson(Object valor) {
        try {
            return json.writeValueAsString(valor);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar el informe", e);
        }
    }

    private <T> List<T> deJson(String valor, TypeReference<List<T>> tipo) {
        if (!hay(valor)) return new ArrayList<>();
        try {
            return new ArrayList<>(json.readValue(valor, tipo));
        } catch (JsonProcessingException e) {
            log.warn("JSON de informe ilegible: {}", valor, e);
            return new ArrayList<>();
        }
    }

    private boolean hay(String s) {
        return s != null && !s.isBlank();
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
