package gob.mdmq.siguba.Controller;

import org.springframework.security.access.prepost.PreAuthorize;
import gob.mdmq.siguba.Entidades.Tramite;
import gob.mdmq.siguba.Exception.ReglaNegocioException;
import gob.mdmq.siguba.Service.DenunciaService;
import gob.mdmq.siguba.dto.DtoNuevaDenuncia;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Alta de denuncias ciudadanas y consulta del proximo codigo. */
@RestController
@RequestMapping("/api")
public class DenunciaController {

    private final DenunciaService denunciaService;
    private final ObjectMapper json;

    public DenunciaController(DenunciaService denunciaService, ObjectMapper json) {
        this.denunciaService = denunciaService;
        this.json = json;
    }

    /** Codigo que tendra la denuncia, para mostrarlo mientras se llena el formulario. */
    @GetMapping("/tramite/codigo-preview")
    @PreAuthorize("@permisos.puede('registrar-denuncia', 'VER')")
    public ResponseEntity<Map<String, String>> codigoPreview() {
        return ResponseEntity.ok(Map.of("codigo", denunciaService.siguienteCodigo()));
    }

    /**
     * Registra una denuncia.
     *
     * Llega como multipart porque incluye imagenes: `datos` es el JSON del
     * formulario e `imagenes` los archivos adjuntos.
     */
    @PostMapping(value = "/denuncia", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@permisos.puede('registrar-denuncia', 'CREAR')")
    public ResponseEntity<Map<String, Object>> registrar(
            @RequestPart("datos") String datosJson,
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes,
            Principal principal) {

        DtoNuevaDenuncia datos;

        try {
            datos = json.readValue(datosJson, DtoNuevaDenuncia.class);
        } catch (Exception e) {
            throw new ReglaNegocioException("Los datos de la denuncia no tienen el formato esperado");
        }

        Tramite tramite = denunciaService.registrar(datos, imagenes,
                principal != null ? principal.getName() : null);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "codigo", tramite.getIdTramite(),
                "idCodigoInterno", tramite.getIdCodigoInterno(),
                "mensaje", "Denuncia registrada correctamente"));
    }
}
