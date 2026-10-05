package gob.mdmq.siguba.Service;

import gob.mdmq.siguba.Exception.ReglaNegocioException;
import gob.mdmq.siguba.Exception.RecursoNoEncontradoException;

import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Guarda archivos en MinIO y devuelve la ruta con la que referenciarlos.
 *
 * La base de datos solo almacena esa ruta (columnas RUTA_IMAGEN de
 * UBA_IMAGEN_DENUNCIA, UBA_IMAGEN_INSPECCION y UBA_UBICACION_IMAGEN); el
 * contenido nunca pasa por SQL Server.
 */
@Service
public class AlmacenamientoService {

    private static final Logger log = LoggerFactory.getLogger(AlmacenamientoService.class);

    /** Solo imagenes y PDF: es lo que la aplicacion adjunta hoy. */
    private static final List<String> TIPOS_PERMITIDOS = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "application/pdf");

    private static final long TAMANO_MAXIMO = 10L * 1024 * 1024;   // 10 MB

    /** Cuanto vale una URL firmada. Suficiente para ver una ficha sin recargar. */
    private static final int VIGENCIA_URL_MINUTOS = 60;

    private static final DateTimeFormatter CARPETA =
            DateTimeFormatter.ofPattern("yyyy/MM", Locale.ROOT);

    private final MinioClient minio;

    /** Solo para firmar: apunta a la URL publica. Ver MinioConfig. */
    private final MinioClient firmante;

    private final String bucket;
    private final String urlPublica;

    public AlmacenamientoService(
            MinioClient minio,
            @Qualifier("minioFirmante") MinioClient firmante,
            @Value("${minio.bucket}") String bucket,
            @Value("${minio.public-url}") String urlPublica) {

        this.minio = minio;
        this.firmante = firmante;
        this.bucket = bucket;
        this.urlPublica = urlPublica.endsWith("/")
                ? urlPublica.substring(0, urlPublica.length() - 1)
                : urlPublica;
    }

    /**
     * Sube un archivo y devuelve su ruta dentro del bucket, que es lo que se
     * guarda en la columna RUTA_IMAGEN.
     *
     * @param carpeta agrupacion logica: "denuncias", "inspecciones", "ubicaciones".
     */
    public String guardar(MultipartFile archivo, String carpeta) {

        if (archivo == null || archivo.isEmpty()) {
            throw new ReglaNegocioException("El archivo esta vacio");
        }

        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new ReglaNegocioException(
                    "El archivo supera el maximo de 10 MB");
        }

        String tipo = archivo.getContentType();

        if (tipo == null || !TIPOS_PERMITIDOS.contains(tipo.toLowerCase(Locale.ROOT))) {
            throw new ReglaNegocioException(
                    "Tipo de archivo no permitido: " + tipo
                    + ". Se aceptan JPG, PNG, WEBP, GIF y PDF.");
        }

        // Nombre propio: el del cliente no es de fiar (puede traer rutas) y dos
        // usuarios pueden subir "foto.jpg" el mismo dia.
        String objeto = "%s/%s/%s%s".formatted(
                limpiar(carpeta),
                LocalDate.now().format(CARPETA),
                UUID.randomUUID(),
                extension(archivo.getOriginalFilename()));

        try (InputStream in = archivo.getInputStream()) {

            minio.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objeto)
                    .stream(in, archivo.getSize(), -1)
                    .contentType(tipo)
                    .build());

        } catch (Exception e) {
            log.error("No se pudo subir {} a MinIO", objeto, e);
            throw new IllegalStateException("No se pudo guardar el archivo", e);
        }

        return objeto;
    }

    /** Abre el contenido de un objeto para devolverlo al cliente. */
    public InputStream leer(String objeto) {
        try {
            return minio.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objeto)
                    .build());

        } catch (Exception e) {
            throw new RecursoNoEncontradoException("No existe el archivo " + objeto);
        }
    }

    /** Tipo MIME guardado junto al objeto. */
    public String tipoDe(String objeto) {
        try {
            StatObjectResponse stat = minio.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(objeto)
                    .build());

            return stat.contentType();

        } catch (Exception e) {
            throw new RecursoNoEncontradoException("No existe el archivo " + objeto);
        }
    }

    public void eliminar(String objeto) {
        try {
            minio.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objeto)
                    .build());

        } catch (Exception e) {
            log.warn("No se pudo eliminar {} de MinIO", objeto, e);
        }
    }

    /**
     * URL con la que el navegador alcanza el archivo.
     *
     * Se firma y caduca: el bucket no es publico, asi que una URL directa daba
     * una imagen rota, y abrirlo al mundo expondria las fotos de todas las
     * denuncias a cualquiera que adivine una ruta. Una etiqueta <img> no puede
     * enviar la cabecera de autorizacion, de ahi la firma.
     */
    public String urlDe(String objeto) {

        if (objeto == null || objeto.isBlank()) {
            return null;
        }

        try {
            // Se firma con el cliente que apunta a la URL publica: la firma
            // incluye el Host, asi que cambiarlo despues la invalidaba y MinIO
            // devolvia 403.
            return firmante.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(objeto)
                    .expiry(VIGENCIA_URL_MINUTOS, TimeUnit.MINUTES)
                    .build());

        } catch (Exception e) {
            log.warn("No se pudo firmar la URL de {}", objeto, e);
            return "%s/%s/%s".formatted(urlPublica, bucket, objeto);
        }
    }

    // ------------------------------------------------------------------ utilidades

    private static String limpiar(String carpeta) {
        if (carpeta == null || carpeta.isBlank()) {
            return "varios";
        }
        // Nada de "..", barras ni caracteres raros: evita escribir fuera de sitio.
        return carpeta.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]", "");
    }

    private static String extension(String nombre) {
        if (nombre == null) return "";
        int punto = nombre.lastIndexOf('.');
        if (punto < 0 || punto == nombre.length() - 1) return "";
        String ext = nombre.substring(punto).toLowerCase(Locale.ROOT);
        return ext.matches("\\.[a-z0-9]{1,5}") ? ext : "";
    }
}
