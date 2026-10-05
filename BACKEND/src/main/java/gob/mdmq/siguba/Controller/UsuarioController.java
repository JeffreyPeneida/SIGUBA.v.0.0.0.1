package gob.mdmq.siguba.Controller;

import gob.mdmq.siguba.Entidades.RolUsuario;
import gob.mdmq.siguba.dto.DtoResponse;
import gob.mdmq.siguba.dto.DtoUsuario;
import gob.mdmq.siguba.Exception.ReglaNegocioException;
import gob.mdmq.siguba.dto.DtoLogin;
import gob.mdmq.siguba.dto.DtoSesion;
import gob.mdmq.siguba.Security.JwtService;
import gob.mdmq.siguba.Security.PermisoService;
import gob.mdmq.siguba.Security.SesionUsuarioService;
import gob.mdmq.siguba.Service.UsuarioService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PermisoService permisos;

    @Autowired
    private SesionUsuarioService sesiones;

    @Autowired
    private JwtService jwtService;

    // ==================================================
    // LISTAR USUARIOS
    // ==================================================
    @GetMapping("/usuarios")
    @PreAuthorize("@permisos.puedeAlguna('usuarios:VER', 'tecnicos:VER')")
    public ResponseEntity<List<DtoUsuario>> listarUsuarios(
            @RequestParam(value = "incluirInactivos", defaultValue = "false") boolean incluirInactivos) {

        try {

            return ResponseEntity.ok(
                    usuarioService.obtenerUsuarios(incluirInactivos));

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Error al consultar usuarios",
                    e);
        }
    }

    // ==================================================
    // BUSCAR POR CEDULA
    // ==================================================
    @GetMapping("/usuarioCedula/{cedula}")
    @PreAuthorize("@permisos.puedeAlguna('usuarios:VER', 'tecnicos:VER')")
    public ResponseEntity<DtoUsuario> obtenerPorCedula(
            @PathVariable("cedula") String cedula) {

        try {

            return ResponseEntity.ok(
                    usuarioService.obtenerPorCedula(cedula));

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Error al consultar usuario",
                    e);
        }
    }

    // ==================================================
    // BUSCAR POR USUARIO
    // ==================================================
    @GetMapping("/usuario/{usuario}")
    @PreAuthorize("@permisos.puedeAlguna('usuarios:VER', 'tecnicos:VER') or #usuario == authentication.name")
    public ResponseEntity<DtoUsuario> obtenerPorUsuario(
            @PathVariable("usuario") String usuario) {

        try {

            return ResponseEntity.ok(
                    usuarioService.obtenerPorUsuario(usuario));

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Error al consultar usuario",
                    e);
        }
    }

    // ==================================================
    // LOGIN
    // ==================================================
    /**
     * Login por POST: las credenciales viajan en el cuerpo, no en la URL.
     * La variante GET /login/{usuario}/{password} se mantiene por
     * compatibilidad, pero deja la contrasena en los logs de acceso, en el
     * historial del navegador y en cualquier proxy intermedio.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody DtoLogin credenciales) {

        if (credenciales == null
                || credenciales.getUsuario() == null
                || credenciales.getUsuario().isBlank()
                || credenciales.getPassword() == null
                || credenciales.getPassword().isBlank()) {

            throw new ReglaNegocioException("Usuario y contrasena son obligatorios");
        }

        DtoUsuario usuario = usuarioService.login(
                credenciales.getUsuario(),
                credenciales.getPassword());

        if (usuario == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new DtoResponse(
                            HttpStatus.UNAUTHORIZED.value(),
                            "Usuario o contrasena incorrectos"));
        }

        // El token lleva dentro el rol; el cliente lo envia en cada peticion.
        String token = usuarioService.generarToken(credenciales.getUsuario());

        return ResponseEntity.ok(
                new DtoSesion(token, jwtService.getDuracionMs(), usuario));
    }

    @GetMapping("/login/{usuario}/{password}")
    public ResponseEntity<DtoUsuario> login(
            @PathVariable("usuario") String usuario,
            @PathVariable("password") String password) {

        try {

            return ResponseEntity.ok(
                    usuarioService.login(usuario, password));

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Error al realizar login",
                    e);
        }
    }

    // ==================================================
    // BUSCAR POR ROL
    // ==================================================
    @GetMapping("/usuariosRol/{rol}")
    @PreAuthorize("@permisos.puedeAlguna('usuarios:VER', 'tecnicos:VER')")
    public ResponseEntity<List<DtoUsuario>> buscarPorRol(
            @PathVariable("rol") RolUsuario rol) {

        try {

            return ResponseEntity.ok(
                    usuarioService.buscarPorRol(rol));

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Error al consultar usuarios por rol",
                    e);
        }
    }

    // ==================================================
    // BUSQUEDA GENERAL
    // ==================================================
    @GetMapping("/buscarUsuario/{texto}")
    @PreAuthorize("@permisos.puedeAlguna('usuarios:VER', 'tecnicos:VER')")
    public ResponseEntity<List<DtoUsuario>> buscar(
            @PathVariable("texto") String texto) {

        try {

            return ResponseEntity.ok(
                    usuarioService.buscar(texto));

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Error al realizar búsqueda",
                    e);
        }
    }

    // ==================================================
    // CREAR USUARIO
    // ==================================================
    @PostMapping("/crearUsuario")
    public ResponseEntity<?> crearUsuario(
            @RequestBody DtoUsuario dtoUsuario) {

        try {

            // El registro es publico, pero elegir el rol no puede serlo: sin
            // esto cualquiera se daba de alta como ADMIN. Solo quien tiene
            // permiso de crear en Usuarios puede asignar otro rol.
            if (!permisos.puede("usuarios", "CREAR")) {
                dtoUsuario.setRol(RolUsuario.USUARIO);
            }

            boolean respuesta =
                    usuarioService.guardarUsuario(dtoUsuario);

            String mensaje;

            if (respuesta) {

                mensaje = "Usuario registrado correctamente";

            } else {

                mensaje = "Error al registrar usuario";
            }

            return new ResponseEntity<>(
                    new DtoResponse(
                            HttpStatus.CREATED.value(),
                            mensaje),
                    HttpStatus.CREATED);

        } catch (Exception e) {

            return new ResponseEntity<>(
                    new DtoResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            e.getMessage(),
                            e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    // ==================================================
    // ACTUALIZAR USUARIO
    // ==================================================
    @PutMapping("/actualizarUsuario/{id}")
    @PreAuthorize("@permisos.puedeAlguna('usuarios:EDITAR', 'tecnicos:EDITAR')")
    public ResponseEntity<?> actualizarUsuario(
            @PathVariable("id") Integer id,
            @RequestBody DtoUsuario dtoUsuario) {

        try {

            // Quitarse el propio rol puede dejar el sistema sin nadie que administre.
            DtoUsuario yo = usuarioService.obtenerPorUsuario(
                    SecurityContextHolder.getContext().getAuthentication().getName());
            if (yo != null && id.equals(yo.getIdUsuario())
                    && dtoUsuario.getRol() != null && dtoUsuario.getRol() != yo.getRol()) {
                throw new ReglaNegocioException("No puedes cambiar tu propio rol");
            }

            boolean respuesta =
                    usuarioService.actualizarUsuario(
                            id,
                            dtoUsuario);

            // Un cambio de rol aplica en la siguiente peticion.
            sesiones.invalidar();

            String mensaje;

            if (respuesta) {

                mensaje = "Usuario actualizado correctamente";

            } else {

                mensaje = "Error al actualizar usuario";
            }

            return ResponseEntity.ok(
                    new DtoResponse(
                            HttpStatus.OK.value(),
                            mensaje));

        } catch (Exception e) {

            return new ResponseEntity<>(
                    new DtoResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            e.getMessage(),
                            e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    // ==================================================
    // ELIMINAR USUARIO
    // ==================================================
    @DeleteMapping("/eliminarUsuario/{cedula}")
    @PreAuthorize("@permisos.puedeAlguna('usuarios:ELIMINAR', 'tecnicos:ELIMINAR')")
    public ResponseEntity<?> eliminarUsuario(
            @PathVariable("cedula") String cedula) {

        try {

            // Desactivarse a uno mismo cerraria la unica sesion que puede deshacerlo.
            if (esUsuarioActual(cedula)) {
                throw new ReglaNegocioException("No puedes desactivar tu propia cuenta");
            }

            boolean respuesta =
                    usuarioService.eliminarUsuario(cedula);

            // La cuenta deja de entrar en la siguiente peticion, no cuando caduque su token.
            sesiones.invalidar();

            String mensaje;

            if (respuesta) {

                mensaje = "Usuario desactivado correctamente";

            } else {

                mensaje = "El usuario no existe o ya estaba inactivo";
            }

            return ResponseEntity.ok(
                    new DtoResponse(
                            HttpStatus.OK.value(),
                            mensaje));

        } catch (Exception e) {

            return new ResponseEntity<>(
                    new DtoResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            e.getMessage(),
                            e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    // ==================================================
    // RESTAURAR USUARIO
    // ==================================================
    @PutMapping("/restaurarUsuario/{cedula}")
    @PreAuthorize("@permisos.puedeAlguna('usuarios:ELIMINAR', 'tecnicos:ELIMINAR')")
    public ResponseEntity<?> restaurarUsuario(
            @PathVariable("cedula") String cedula) {

        try {

            boolean respuesta =
                    usuarioService.restaurarUsuario(cedula);

            sesiones.invalidar();

            String mensaje;

            if (respuesta) {

                mensaje = "Usuario activado correctamente";

            } else {

                mensaje = "El usuario no existe o ya estaba activo";
            }

            return ResponseEntity.ok(
                    new DtoResponse(
                            HttpStatus.OK.value(),
                            mensaje));

        } catch (Exception e) {

            return new ResponseEntity<>(
                    new DtoResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            e.getMessage(),
                            e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    /** True si la cedula es la de quien tiene la sesion abierta. */
    private boolean esUsuarioActual(String cedula) {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || cedula == null) return false;

        DtoUsuario yo = usuarioService.obtenerPorUsuario(auth.getName());
        return yo != null && cedula.equals(yo.getCedula());
    }
}