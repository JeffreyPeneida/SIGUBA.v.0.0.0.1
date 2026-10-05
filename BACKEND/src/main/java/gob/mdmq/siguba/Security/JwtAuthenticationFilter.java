package gob.mdmq.siguba.Security;

import io.jsonwebtoken.Claims;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lee el token del encabezado Authorization y, si es valido, deja al usuario
 * autenticado en el contexto de Spring Security con su rol como authority.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final SesionUsuarioService sesiones;

    public JwtAuthenticationFilter(JwtService jwtService, SesionUsuarioService sesiones) {
        this.jwtService = jwtService;
        this.sesiones = sesiones;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String encabezado = request.getHeader("Authorization");

        if (encabezado != null && encabezado.startsWith(PREFIJO)) {

            Claims claims = jwtService.validar(encabezado.substring(PREFIJO.length()));

            if (claims != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // El rol se toma de la base, no del token: asi un usuario
                // desactivado deja de entrar y un cambio de rol aplica sin
                // esperar a que el token caduque. Cuenta inactiva = sin sesion.
                var vigente = sesiones.rolSiActivo(claims.getSubject());

                if (vigente.isEmpty()) {
                    chain.doFilter(request, response);
                    return;
                }

                String rol = vigente.get();

                // Spring Security espera el prefijo ROLE_ para hasRole(...).
                var authorities = rol == null
                        ? List.<SimpleGrantedAuthority>of()
                        : List.of(new SimpleGrantedAuthority("ROLE_" + rol));

                var auth = new UsernamePasswordAuthenticationToken(
                        claims.getSubject(), null, authorities);

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }
}
