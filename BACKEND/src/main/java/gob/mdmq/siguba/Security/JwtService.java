package gob.mdmq.siguba.Security;

import gob.mdmq.siguba.Entidades.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Emite y valida los tokens JWT que identifican al usuario en cada peticion. */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long duracionMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secreto,
            @Value("${app.jwt.expiration-minutes:480}") long minutos) {

        // HS256 exige al menos 256 bits de clave.
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "app.jwt.secret debe tener al menos 32 caracteres");
        }

        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.duracionMs = minutos * 60_000L;
    }

    public String generar(Usuario usuario) {

        Date ahora = new Date();

        return Jwts.builder()
                .subject(usuario.getUsuario())
                .claim("idUsuario", usuario.getIdUsuario())
                .claim("rol", usuario.getRol() != null ? usuario.getRol().name() : null)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + duracionMs))
                .signWith(clave)
                .compact();
    }

    /** Devuelve los claims si el token es valido; null si no lo es. */
    public Claims validar(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(clave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (JwtException | IllegalArgumentException e) {
            // Token invalido, caducado o manipulado: se trata como no autenticado.
            return null;
        }
    }

    public long getDuracionMs() {
        return duracionMs;
    }
}
