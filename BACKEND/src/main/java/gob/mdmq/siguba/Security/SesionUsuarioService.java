package gob.mdmq.siguba.Security;

import gob.mdmq.siguba.Repository.UsuarioRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

/**
 * Rol vigente de quien trae un token, y si su cuenta sigue activa.
 *
 * El JWT dura 8 horas y lleva el rol dentro. Fiarse solo de el significaba que
 * un usuario desactivado seguia entrando hasta que el token caducara, y que un
 * cambio de rol no aplicaba hasta volver a iniciar sesion. Aqui se consulta la
 * base, con una cache corta para no hacerlo en cada peticion.
 */
@Service
public class SesionUsuarioService {

    /** Cuanto puede tardar en aplicar un cambio que no pase por invalidar(). */
    private static final long VIGENCIA_MS = 30_000;

    private record Entrada(Optional<String> rol, long hasta) {}

    private final UsuarioRepository usuarios;
    private final Map<String, Entrada> cache = new ConcurrentHashMap<>();

    public SesionUsuarioService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    /** Rol actual del usuario si esta ACTIVO; vacio si no existe o esta inactivo. */
    public Optional<String> rolSiActivo(String nombreUsuario) {

        long ahora = System.currentTimeMillis();
        Entrada e = cache.get(nombreUsuario);

        if (e == null || e.hasta() < ahora) {
            // buscarPorUsuario solo devuelve cuentas con ESTADO = 'ACTIVO'.
            Optional<String> rol = usuarios.buscarPorUsuario(nombreUsuario)
                    .map(u -> u.getRol() != null ? u.getRol().name() : null);
            e = new Entrada(rol, ahora + VIGENCIA_MS);
            cache.put(nombreUsuario, e);
        }

        return e.rol();
    }

    /** Tras activar, desactivar o cambiar el rol de alguien: aplica en la siguiente peticion. */
    public void invalidar() {
        cache.clear();
    }
}
