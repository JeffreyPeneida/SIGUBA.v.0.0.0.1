package gob.mdmq.siguba.Backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// Añadimos el paquete raíz común para que escanee "Controller", "Service", "repository", etc.
@SpringBootApplication(scanBasePackages = "gob.mdmq.siguba")
@EnableJpaRepositories(basePackages = "gob.mdmq.siguba.Repository") // <-- Añade esto si falla el autowired del repositorio
@EntityScan(basePackages = "gob.mdmq.siguba.Entidades")
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
