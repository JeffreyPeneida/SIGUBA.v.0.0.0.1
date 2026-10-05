package gob.mdmq.siguba.Config;

import gob.mdmq.siguba.Security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Autenticacion por JWT, sin sesion en el servidor.
 *
 * Las reglas por rol se declaran aqui para las rutas y con @PreAuthorize en los
 * metodos que lo necesiten. @EnableMethodSecurity activa esa segunda via.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // La API no usa cookies de sesion, asi que CSRF no aplica.
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Sin esto, una peticion sin token recibiria una redireccion al
            // formulario de login de Spring en vez de un 401.
            .exceptionHandling(e -> e
                    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

            .authorizeHttpRequests(auth -> auth

                // Publicas: iniciar sesion y registrarse.
                .requestMatchers("/api/login", "/api/login/**").permitAll()
                .requestMatchers("/api/crearUsuario").permitAll()

                // El formulario de registro es publico y necesita este
                // catalogo para su desplegable. Es informacion publica: las
                // administraciones zonales del Distrito.
                .requestMatchers(org.springframework.http.HttpMethod.GET,
                        "/api/adminzonal/buscarAdministracionesZonales",
                        "/api/adminzonal/buscarNombresAdministracionesZonales").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                // Lo que puede hacer cada rol ya no se fija aqui: lo decide
                // UBA_ROL_PERMISO (pantalla "Perfiles") mediante
                // @PreAuthorize("@permisos.puede(...)") en cada controlador.

                // Catalogos del formulario de denuncia: cualquiera con sesion.
                // Se listan aparte para que quede claro que son de lectura.
                .requestMatchers(org.springframework.http.HttpMethod.GET,
                        "/api/parroquia/**", "/api/barrio/**",
                        "/api/tipo-denunciante", "/api/dependencia",
                        "/api/predio", "/api/tramite/codigo-preview").authenticated()

                // Catalogos y tramites: cualquier usuario autenticado.
                .requestMatchers("/api/**").authenticated()

                .anyRequest().permitAll())

            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
