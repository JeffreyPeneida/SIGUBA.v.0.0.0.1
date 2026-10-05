package gob.mdmq.siguba.Controller;

import gob.mdmq.siguba.Service.AlmacenamientoService;
import gob.mdmq.siguba.dto.DtoArchivo;
import gob.mdmq.siguba.dto.DtoResponse;

import java.io.InputStream;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Subida y descarga de imagenes y documentos.
 *
 * La respuesta de la subida trae la `ruta` que hay que guardar en la columna
 * RUTA_IMAGEN correspondiente, y la `url` con la que el navegador la muestra.
 */
@RestController
@RequestMapping("/api/archivos")
public class ArchivoController {

    private final AlmacenamientoService almacenamiento;

    public ArchivoController(AlmacenamientoService almacenamiento) {
        this.almacenamiento = almacenamiento;
    }

    /** Sube un archivo a la carpeta indicada (denuncias, inspecciones, ubicaciones). */
    @PostMapping(value = "/{carpeta}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DtoArchivo> subir(
            @PathVariable("carpeta") String carpeta,
            @RequestPart("archivo") MultipartFile archivo) {

        String ruta = almacenamiento.guardar(archivo, carpeta);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new DtoArchivo(
                        ruta,
                        almacenamiento.urlDe(ruta),
                        archivo.getOriginalFilename(),
                        archivo.getSize()));
    }

    /**
     * Devuelve el contenido. Sirve como alternativa a la URL directa de MinIO
     * cuando el bucket no es publico.
     */
    @GetMapping("/**")
    public ResponseEntity<InputStreamResource> descargar(
            @RequestParam("ruta") String ruta) {

        InputStream contenido = almacenamiento.leer(ruta);
        String tipo = almacenamiento.tipoDe(ruta);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        tipo != null ? tipo : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(new InputStreamResource(contenido));
    }

    /** Borrar un archivo queda reservado a la administracion. */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DtoResponse> eliminar(@RequestParam("ruta") String ruta) {

        almacenamiento.eliminar(ruta);

        return ResponseEntity.ok(
                new DtoResponse(HttpStatus.OK.value(), "Archivo eliminado"));
    }
}
