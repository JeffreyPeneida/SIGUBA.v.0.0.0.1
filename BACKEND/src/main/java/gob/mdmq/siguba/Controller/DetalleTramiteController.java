package gob.mdmq.siguba.Controller;

import org.springframework.security.access.prepost.PreAuthorize;
import gob.mdmq.siguba.Entidades.*;
import gob.mdmq.siguba.Exception.RecursoNoEncontradoException;
import gob.mdmq.siguba.Repository.ImagenDenunciaRepository;
import gob.mdmq.siguba.Repository.TramiteRepository;
import gob.mdmq.siguba.Service.AlmacenamientoService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Detalle de un tramite para la pantalla de inspeccion.
 *
 * Las claves salen en snake_case porque es lo que ya consume esa pantalla
 * (id_codigo_interno, ruta_imagen...). Aplanar aqui evita que el frontend tenga
 * que recorrer relaciones anidadas para pintar una ficha.
 */
@RestController
@RequestMapping("/api")
public class DetalleTramiteController {

    private final TramiteRepository tramiteRepository;
    private final ImagenDenunciaRepository imagenDenunciaRepository;
    private final AlmacenamientoService almacenamiento;

    public DetalleTramiteController(TramiteRepository tramiteRepository,
                                    ImagenDenunciaRepository imagenDenunciaRepository,
                                    AlmacenamientoService almacenamiento) {
        this.tramiteRepository = tramiteRepository;
        this.imagenDenunciaRepository = imagenDenunciaRepository;
        this.almacenamiento = almacenamiento;
    }

    /** Ficha completa de un tramite por su codigo interno. */
    @GetMapping("/tramite/{id}")
    @PreAuthorize("@accesoTramite.puedeVer(#id)")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> detalle(@PathVariable("id") Long id) {

        Tramite t = tramiteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el trámite " + id));

        Map<String, Object> d = new LinkedHashMap<>();

        d.put("id_codigo_interno", t.getIdCodigoInterno());
        d.put("id_tramite", t.getIdTramite());
        d.put("fecha_denuncia", t.getFechaDenuncia());
        d.put("narracion", t.getNarracion());
        d.put("latitud", t.getLatitud());
        d.put("longitud", t.getLongitud());
        d.put("fecha_inspeccion", t.getFechaInspeccion());
        d.put("hora_inspeccion", t.getHoraInspeccion());
        d.put("descripcion", t.getDescripcion());
        d.put("conclusiones", t.getConclusiones());
        d.put("recomendaciones", t.getRecomendaciones());
        d.put("estado", t.getEstado());

        Denunciante den = t.getDenunciante();
        d.put("nombre", den != null ? den.getNombre() : null);
        d.put("apellido", den != null ? den.getApellido() : null);
        d.put("cedula", den != null ? den.getCedula() : null);
        d.put("telefono", den != null ? den.getTelefono() : null);
        d.put("tipo_denunciante", den != null && den.getTipoDenunciante() != null
                ? den.getTipoDenunciante().getNombre() : null);

        Tecnico tec = t.getTecnico();
        d.put("id_tecnico", tec != null ? tec.getIdTecnico() : null);
        d.put("tecnico", tec != null
                ? (tec.getNombre() + " " + tec.getApellido()).trim() : null);

        Ubicacion u = t.getUbicacion();
        d.put("direccion", u != null ? u.getDireccion() : null);
        d.put("referencia", u != null ? u.getReferencia() : null);
        d.put("admin_zonal", u != null && u.getAdminZonal() != null
                ? u.getAdminZonal().getNombre() : null);
        d.put("parroquia", u != null && u.getParroquia() != null
                ? u.getParroquia().getNombre() : null);
        d.put("predio", u != null && u.getPredio() != null
                ? u.getPredio().getNombre() : null);

        d.put("barrio", t.getBarrio() != null ? t.getBarrio().getNombre() : null);
        d.put("dependencia", t.getDependencia() != null
                ? t.getDependencia().getNombre() : null);
        d.put("area", t.getAreaInspeccion() != null
                ? t.getAreaInspeccion().getNombre() : null);
        d.put("motivo", t.getMotivoInspeccion() != null
                ? t.getMotivoInspeccion().getNombre() : null);

        // Las especies van tambien en la ficha: son la plaga a tratar.
        List<String> especies = new ArrayList<>();
        if (t.getEspecies() != null) {
            t.getEspecies().forEach(e -> especies.add(e.getNombre()));
        }
        d.put("especies", especies);

        return ResponseEntity.ok(d);
    }

    /** Especies denunciadas, para decidir que pestañas mostrar. */
    @GetMapping("/tramite-especie/{id}")
    @PreAuthorize("@accesoTramite.puedeVer(#id)")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> especies(@PathVariable("id") Long id) {

        Tramite t = tramiteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el trámite " + id));

        List<Map<String, Object>> salida = new ArrayList<>();

        if (t.getEspecies() != null) {
            for (Especie e : t.getEspecies()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id_especie", e.getIdEspecie());
                m.put("nombre", e.getNombre());
                salida.add(m);
            }
        }

        return ResponseEntity.ok(salida);
    }

    /** Fotos que adjunto el denunciante. */
    @GetMapping("/imagen-denuncia/{id}")
    @PreAuthorize("@accesoTramite.puedeVer(#id)")
    public ResponseEntity<List<Map<String, Object>>> imagenesDenuncia(@PathVariable("id") Long id) {

        List<Map<String, Object>> salida = new ArrayList<>();

        for (ImagenDenuncia img : imagenDenunciaRepository.obtenerPorTramite(id)) {
            Map<String, Object> m = new LinkedHashMap<>();
            // La ruta es lo que se guarda; la url es con la que el navegador la pide.
            m.put("ruta_imagen", almacenamiento.urlDe(img.getRutaImagen()));
            m.put("ruta", img.getRutaImagen());
            salida.add(m);
        }

        return ResponseEntity.ok(salida);
    }

    /**
     * Fotos tomadas durante la inspeccion.
     *
     * Todavia no se guarda ninguna: la pantalla de inspeccion no las sube. Se
     * responde una lista vacia para que la ficha cargue en vez de romperse con
     * un 404, y queda pendiente implementarlo.
     */
    @GetMapping("/imagen-inspeccion/{id}")
    @PreAuthorize("@accesoTramite.puedeVer(#id)")
    public ResponseEntity<List<Map<String, Object>>> imagenesInspeccion(@PathVariable("id") Long id) {
        return ResponseEntity.ok(List.of());
    }
}
