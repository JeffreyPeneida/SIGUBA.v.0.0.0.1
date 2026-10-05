package gob.mdmq.siguba.Config;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Las entidades usan FetchType.LAZY en todas sus asociaciones. Sin este modulo
 * Jackson intenta serializar el proxy de Hibernate directamente y la respuesta
 * falla con "HibernateProxy cannot be cast to LazyInitializer".
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Hibernate6Module hibernate6Module() {
        return new Hibernate6Module()
                // Serializa las asociaciones lazy ya cargadas; las no cargadas
                // salen como null en lugar de romper la peticion.
                .configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, false);
    }
}
