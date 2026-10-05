package gob.mdmq.siguba.Security;

import gob.mdmq.siguba.Entidades.Usuario;
import gob.mdmq.siguba.Repository.UsuarioRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cifra al arrancar las contrasenas que todavia esten en texto plano.
 *
 * El proyecto las guardaba sin cifrar y el script de datos de prueba las siembra
 * legibles a proposito. Una contrasena ya cifrada empieza por "$2" (BCrypt), asi
 * que la migracion es idempotente: en el segundo arranque no encuentra nada.
 */
@Component
public class PasswordMigrationRunner implements ApplicationRunner {

    private static final Logger log =
            LoggerFactory.getLogger(PasswordMigrationRunner.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordMigrationRunner(UsuarioRepository usuarioRepository,
                                   PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        int migradas = 0;

        for (Usuario u : usuarioRepository.findAll()) {

            String actual = u.getPassword();

            if (actual == null || actual.isBlank() || actual.startsWith("$2")) {
                continue;
            }

            u.setPassword(passwordEncoder.encode(actual));
            usuarioRepository.save(u);
            migradas++;
        }

        if (migradas > 0) {
            log.warn("Cifradas {} contrasenas que estaban en texto plano.", migradas);
        }
    }
}
