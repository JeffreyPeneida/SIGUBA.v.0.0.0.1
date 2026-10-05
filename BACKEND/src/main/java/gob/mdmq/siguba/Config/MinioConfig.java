package gob.mdmq.siguba.Config;

import io.minio.MinioClient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/** Clientes de MinIO, el almacen de imagenes y archivos. */
@Configuration
public class MinioConfig {

    /** MinIO usa esta region por defecto; fijarla evita ir a preguntarla. */
    private static final String REGION = "us-east-1";

    /** Cliente de trabajo: habla con MinIO por la red interna de Docker. */
    @Bean
    @Primary
    public MinioClient minioClient(
            @Value("${minio.url}") String url,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey) {

        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * Cliente usado SOLO para firmar URLs.
     *
     * La firma AWS SigV4 incluye el Host, asi que no se puede firmar contra
     * minio:9000 y luego cambiar el host por localhost: la firma deja de valer
     * y MinIO responde 403. Firmar es un calculo local, sin red, de modo que un
     * cliente apuntando a la URL publica produce una firma valida para el
     * navegador aunque ese host no se resuelva desde el contenedor.
     */
    @Bean
    @Qualifier("minioFirmante")
    public MinioClient minioFirmante(
            @Value("${minio.public-url}") String urlPublica,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey) {

        return MinioClient.builder()
                .endpoint(urlPublica)
                .credentials(accessKey, secretKey)
                // Sin region fija, firmar dispara una llamada de red para
                // averiguarla, y este endpoint no se resuelve desde dentro del
                // contenedor: fallaba y la URL salia sin firma.
                .region(REGION)
                .build();
    }
}
