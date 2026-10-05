package gob.mdmq.siguba.Security;

import gob.mdmq.siguba.Entidades.RolUsuario;
import gob.mdmq.siguba.Exception.ReglaNegocioException;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Que puede hacer cada rol en cada pantalla.
 *
 * Los permisos viven en UBA_ROL_PERMISO y se editan desde "Perfiles". Antes el
 * acceso estaba escrito a mano en el menu, en las rutas y con hasRole(...) en
 * cada controlador: cambiarlo exigia tocar codigo en los tres sitios.
 *
 * Se usa desde @PreAuthorize con el nombre del bean:
 * {@code @PreAuthorize("@permisos.puede('catalogos', 'CREAR')")}.
 *
 * Se cachean en memoria porque se consultan en cada peticion; guardar invalida
 * la cache, asi que un cambio aplica en la siguiente llamada.
 */
@Service("permisos")
public class PermisoService {

    public static final List<String> ACCIONES = List.of("VER", "CREAR", "EDITAR", "ELIMINAR");

    /** Pantalla que nunca se le puede quitar al ADMIN: sin ella nadie podria devolver los permisos. */
    private static final String PANTALLA_PERFILES = "perfiles";

    private final NamedParameterJdbcTemplate jdbc;

    /** rol -> pantalla -> acciones permitidas. */
    private volatile Map<String, Map<String, Set<String>>> cache;

    public PermisoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ------------------------------------------------------------ consultas

    /** True si el rol de la sesion actual puede hacer la accion en la pantalla. */
    public boolean puede(String pantalla, String accion) {
        String rol = rolActual();
        return rol != null && puede(rol, pantalla, accion);
    }

    /** True si puede alguna de las combinaciones "pantalla:ACCION". */
    public boolean puedeAlguna(String... pares) {
        String rol = rolActual();
        if (rol == null) return false;

        for (String par : pares) {
            String[] p = par.split(":");
            if (p.length == 2 && puede(rol, p[0], p[1])) return true;
        }
        return false;
    }

    public boolean puede(String rol, String pantalla, String accion) {
        return permisos()
                .getOrDefault(rol, Map.of())
                .getOrDefault(pantalla, Set.of())
                .contains(accion.toUpperCase(Locale.ROOT));
    }

    /** Pantallas del rol con lo que puede hacer en cada una, para armar menu y botones. */
    public List<Map<String, Object>> deRol(String rol) {

        Map<String, Set<String>> delRol = permisos().getOrDefault(rol, Map.of());

        return pantallas().stream()
                .map(p -> {
                    Map<String, Object> fila = new LinkedHashMap<>(p);
                    Set<String> acciones = delRol.getOrDefault((String) p.get("clave"), Set.of());
                    ACCIONES.forEach(a -> fila.put(a.toLowerCase(Locale.ROOT), acciones.contains(a)));
                    return fila;
                })
                .collect(Collectors.toList());
    }

    /** Todas las pantallas con los permisos de los tres roles, para la pantalla de Perfiles. */
    public Map<String, Object> matriz() {

        Map<String, Object> roles = new LinkedHashMap<>();
        for (RolUsuario r : RolUsuario.values()) {
            roles.put(r.name(), deRol(r.name()));
        }

        return Map.of(
                "acciones", ACCIONES,
                "roles", roles);
    }

    // ------------------------------------------------------------- guardado

    /**
     * Reemplaza los permisos de un rol.
     *
     * Solo se aceptan acciones que la pantalla declara (no tiene sentido
     * "crear" en Inicio), y el ADMIN conserva siempre ver y editar Perfiles.
     */
    @Transactional
    public void guardar(String rol, List<Map<String, Object>> filas) {

        String rolValido = Arrays.stream(RolUsuario.values())
                .map(Enum::name)
                .filter(r -> r.equalsIgnoreCase(rol))
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException("Rol desconocido: " + rol));

        Map<String, Set<String>> declaradas = pantallas().stream().collect(Collectors.toMap(
                p -> (String) p.get("clave"),
                p -> Set.of(((String) p.get("accionesDisponibles")).split(","))));

        for (Map<String, Object> fila : filas == null ? List.<Map<String, Object>>of() : filas) {

            String pantalla = String.valueOf(fila.get("clave"));
            Set<String> disponibles = declaradas.get(pantalla);

            if (disponibles == null) {
                throw new ReglaNegocioException("Pantalla desconocida: " + pantalla);
            }

            Map<String, Boolean> valores = new HashMap<>();
            for (String a : ACCIONES) {
                boolean marcado = Boolean.TRUE.equals(fila.get(a.toLowerCase(Locale.ROOT)));
                valores.put(a, marcado && disponibles.contains(a));
            }

            // Crear, editar o eliminar sin poder entrar a la pantalla no sirve de nada.
            if (!valores.get("VER")) {
                valores.replaceAll((a, v) -> false);
            }

            if ("ADMIN".equals(rolValido) && PANTALLA_PERFILES.equals(pantalla)) {
                valores.put("VER", true);
                valores.put("EDITAR", true);
            }

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("rol", rolValido)
                    .addValue("pantalla", pantalla)
                    .addValue("ver", valores.get("VER"))
                    .addValue("crear", valores.get("CREAR"))
                    .addValue("editar", valores.get("EDITAR"))
                    .addValue("eliminar", valores.get("ELIMINAR"));

            int actualizadas = jdbc.update("""
                    UPDATE PROYECTO_UBA.UBA_ROL_PERMISO
                       SET VER = :ver, CREAR = :crear, EDITAR = :editar, ELIMINAR = :eliminar
                     WHERE ROL = :rol AND PANTALLA = :pantalla
                    """, params);

            if (actualizadas == 0) {
                jdbc.update("""
                        INSERT INTO PROYECTO_UBA.UBA_ROL_PERMISO (ROL, PANTALLA, VER, CREAR, EDITAR, ELIMINAR)
                        VALUES (:rol, :pantalla, :ver, :crear, :editar, :eliminar)
                        """, params);
            }
        }

        invalidar();
    }

    // ------------------------------------------------------ menu lateral

    /** Pantallas con su configuracion de menu, para Perfiles > Menu lateral. */
    public List<Map<String, Object>> listarPantallas() {
        return pantallas();
    }

    /**
     * Guarda nombre, icono, seccion, orden y visibilidad de las pantallas.
     *
     * La ruta y las acciones no se tocan: dependen del codigo. Perfiles no se
     * puede ocultar ni desactivar, porque es desde donde se deshace el cambio.
     */
    @Transactional
    public void guardarPantallas(List<Map<String, Object>> filas) {

        Set<String> existentes = pantallas().stream()
                .map(p -> (String) p.get("clave"))
                .collect(Collectors.toSet());

        for (Map<String, Object> fila : filas == null ? List.<Map<String, Object>>of() : filas) {

            String clave = String.valueOf(fila.get("clave"));
            if (!existentes.contains(clave)) {
                throw new ReglaNegocioException("Pantalla desconocida: " + clave);
            }

            String nombre = texto(fila.get("nombre"));
            String icono = texto(fila.get("icono"));
            String seccion = texto(fila.get("seccion"));

            if (nombre.isEmpty() || nombre.length() > 80) {
                throw new ReglaNegocioException("El nombre de la pantalla es obligatorio (máximo 80 caracteres)");
            }
            if (seccion.isEmpty() || seccion.length() > 40) {
                throw new ReglaNegocioException("La sección es obligatoria (máximo 40 caracteres)");
            }
            // Solo clases de Font Awesome: el valor acaba dentro de un atributo class.
            if (!icono.matches("fa-[a-z0-9-]{1,37}")) {
                throw new ReglaNegocioException("Icono no válido: " + icono);
            }

            int orden;
            try {
                orden = Integer.parseInt(String.valueOf(fila.get("orden")));
            } catch (NumberFormatException e) {
                throw new ReglaNegocioException("Orden no válido en " + nombre);
            }

            boolean esPerfiles = PANTALLA_PERFILES.equals(clave);
            boolean enMenu = esPerfiles || !Boolean.FALSE.equals(fila.get("enMenu"));
            boolean activa = esPerfiles || !Boolean.FALSE.equals(fila.get("activa"));

            jdbc.update("""
                    UPDATE PROYECTO_UBA.UBA_PANTALLA
                       SET NOMBRE = :nombre, ICONO = :icono, SECCION = :seccion,
                           ORDEN = :orden, EN_MENU = :enMenu, ACTIVA = :activa
                     WHERE CLAVE = :clave
                    """, new MapSqlParameterSource()
                    .addValue("clave", clave)
                    .addValue("nombre", nombre)
                    .addValue("icono", icono)
                    .addValue("seccion", seccion)
                    .addValue("orden", orden)
                    .addValue("enMenu", enMenu)
                    .addValue("activa", activa));
        }

        invalidar();
    }

    private static String texto(Object valor) {
        return valor == null ? "" : String.valueOf(valor).trim();
    }

    public void invalidar() {
        cache = null;
    }

    // ------------------------------------------------------------ internos

    private List<Map<String, Object>> pantallas() {
        return jdbc.query("""
                SELECT CLAVE, NOMBRE, RUTA, ICONO, SECCION, ORDEN, ACCIONES, EN_MENU, ACTIVA
                  FROM PROYECTO_UBA.UBA_PANTALLA
                 ORDER BY ORDEN, NOMBRE
                """, (rs, i) -> {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("clave", rs.getString("CLAVE"));
            p.put("nombre", rs.getString("NOMBRE"));
            p.put("ruta", rs.getString("RUTA"));
            p.put("icono", rs.getString("ICONO"));
            p.put("seccion", rs.getString("SECCION"));
            p.put("orden", rs.getInt("ORDEN"));
            p.put("accionesDisponibles", rs.getString("ACCIONES"));
            p.put("enMenu", rs.getBoolean("EN_MENU"));
            p.put("activa", rs.getBoolean("ACTIVA"));
            return p;
        });
    }

    private Map<String, Map<String, Set<String>>> permisos() {

        Map<String, Map<String, Set<String>>> actual = cache;
        if (actual != null) return actual;

        Map<String, Map<String, Set<String>>> nuevo = new HashMap<>();

        jdbc.query("""
                SELECT r.ROL, r.PANTALLA, r.VER, r.CREAR, r.EDITAR, r.ELIMINAR
                  FROM PROYECTO_UBA.UBA_ROL_PERMISO r
                  JOIN PROYECTO_UBA.UBA_PANTALLA p ON p.CLAVE = r.PANTALLA
                 -- Una pantalla desactivada no da acceso a nadie, sea cual sea su permiso.
                 WHERE p.ACTIVA = 1
                """, rs -> {
            Set<String> acciones = new HashSet<>();
            for (String a : ACCIONES) {
                if (rs.getBoolean(a)) acciones.add(a);
            }
            nuevo.computeIfAbsent(rs.getString("ROL"), r -> new HashMap<>())
                 .put(rs.getString("PANTALLA"), acciones);
        });

        cache = nuevo;
        return nuevo;
    }

    /** Rol de la sesion, sacado de la autoridad ROLE_x que pone JwtAuthenticationFilter. */
    public static String rolActual() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .findFirst()
                .orElse(null);
    }
}
