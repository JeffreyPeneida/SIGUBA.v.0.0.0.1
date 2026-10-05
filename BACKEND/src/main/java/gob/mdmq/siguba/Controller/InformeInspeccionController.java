package gob.mdmq.siguba.Controller;

import gob.mdmq.siguba.Service.InformeInspeccionService;
import gob.mdmq.siguba.Service.InformeInspeccionService.InformeExportable;
import gob.mdmq.siguba.Service.InformePdfService;
import gob.mdmq.siguba.Service.InformeWordService;
import gob.mdmq.siguba.dto.DtoInformeInspeccion;

import java.security.Principal;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Informe tecnico de la inspeccion (modelo oficial UBA).
 *
 * Lo ven, editan y exportan quienes gestionan tramites: el tecnico y la
 * administracion. El ciudadano no: el informe tiene datos internos.
 */
@RestController
@RequestMapping("/api/informe-inspeccion")
public class InformeInspeccionController {

    private static final MediaType DOCX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    private static final String GESTIONA =
            "@permisos.puedeAlguna('inspecciones:VER', 'control-plagas:VER')";

    private final InformeInspeccionService informes;
    private final InformeWordService word;
    private final InformePdfService pdf;

    public InformeInspeccionController(InformeInspeccionService informes,
                                       InformeWordService word,
                                       InformePdfService pdf) {
        this.informes = informes;
        this.word = word;
        this.pdf = pdf;
    }

    /** Informe guardado, o el borrador armado con los datos del tramite. */
    @GetMapping("/{id}")
    @PreAuthorize(GESTIONA)
    public DtoInformeInspeccion obtener(@PathVariable("id") Long id) {
        return informes.obtener(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisos.puede('inspecciones', 'CREAR')")
    public DtoInformeInspeccion guardar(@PathVariable("id") Long id,
                                        @RequestBody DtoInformeInspeccion dto,
                                        Principal usuario) {
        return informes.guardar(id, dto, usuario != null ? usuario.getName() : null);
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize(GESTIONA)
    public ResponseEntity<byte[]> pdf(@PathVariable("id") Long id) {
        InformeExportable e = informes.exportable(id);
        return archivo(pdf.generar(e), MediaType.APPLICATION_PDF, nombre(e, "pdf"));
    }

    @GetMapping("/{id}/word")
    @PreAuthorize(GESTIONA)
    public ResponseEntity<byte[]> word(@PathVariable("id") Long id) {
        InformeExportable e = informes.exportable(id);
        return archivo(word.generar(e), DOCX, nombre(e, "docx"));
    }

    private static String nombre(InformeExportable e, String extension) {
        String codigo = e.informe().getCodigoTramite() != null
                ? e.informe().getCodigoTramite()
                : String.valueOf(e.informe().getIdCodigoInterno());
        return "Informe_inspeccion_" + codigo.replaceAll("[^A-Za-z0-9_-]", "_") + "." + extension;
    }

    private static ResponseEntity<byte[]> archivo(byte[] contenido, MediaType tipo, String nombre) {
        return ResponseEntity.ok()
                .contentType(tipo)
                // El nombre ya viene saneado a ASCII: no hace falta codificarlo.
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(nombre).build().toString())
                // El frontend lee el nombre del archivo de esta cabecera.
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .body(contenido);
    }
}
