package gob.mdmq.siguba.Security;

import gob.mdmq.siguba.Repository.TramiteRepository;
import gob.mdmq.siguba.Repository.UsuarioRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Quien puede consultar un tramite concreto.
 *
 * Quien gestiona (Control de plagas o Inspecciones) ve cualquiera; el resto,
 * solo los suyos: los que registro o los hechos con su cedula. Antes bastaba
 * con cambiar el numero en /denuncia/{id} para leer la denuncia de otra persona.
 *
 * Uso: {@code @PreAuthorize("@accesoTramite.puedeVer(#id)")}.
 */
@Service("accesoTramite")
public class AccesoTramiteService {

    private final PermisoService permisos;
    private final TramiteRepository tramites;
    private final UsuarioRepository usuarios;

    public AccesoTramiteService(PermisoService permisos, TramiteRepository tramites,
                                UsuarioRepository usuarios) {
        this.permisos = permisos;
        this.tramites = tramites;
        this.usuarios = usuarios;
    }

    public boolean puedeVer(Long idCodigoInterno) {

        if (idCodigoInterno == null) return false;
        if (gestiona()) return true;

        return usuarioActual() != null && usuarios.buscarPorUsuario(usuarioActual())
                .map(u -> tramites.esDeUsuario(idCodigoInterno, u.getIdUsuario(), u.getCedula()))
                .orElse(false);
    }

    public boolean puedeVerCodigo(String codigo) {

        if (codigo == null) return false;
        if (gestiona()) return true;

        return tramites.buscarIdInternoPorCodigo(codigo)
                .map(this::puedeVer)
                .orElse(false);
    }

    private boolean gestiona() {
        return permisos.puedeAlguna("control-plagas:VER", "inspecciones:VER");
    }

    private static String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() ? auth.getName() : null;
    }
}
