package gob.mdmq.siguba.Controller;

import gob.mdmq.siguba.Security.PermisoService;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Perfilamiento: pantallas y acciones permitidas por rol. */
@RestController
@RequestMapping("/api/permisos")
public class PermisoController {

    private final PermisoService permisos;

    public PermisoController(PermisoService permisos) {
        this.permisos = permisos;
    }

    /** Lo que puede hacer quien tiene la sesion abierta: arma su menu, rutas y botones. */
    @GetMapping("/mios")
    public ResponseEntity<List<Map<String, Object>>> mios() {

        String rol = PermisoService.rolActual();

        return rol == null
                ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
                : ResponseEntity.ok(permisos.deRol(rol));
    }

    @GetMapping
    @PreAuthorize("@permisos.puede('perfiles', 'VER')")
    public ResponseEntity<Map<String, Object>> matriz() {
        return ResponseEntity.ok(permisos.matriz());
    }

    /** Configuracion del menu lateral: nombre, icono, seccion, orden y visibilidad. */
    @GetMapping("/pantallas")
    @PreAuthorize("@permisos.puede('perfiles', 'VER')")
    public ResponseEntity<List<Map<String, Object>>> pantallas() {
        return ResponseEntity.ok(permisos.listarPantallas());
    }

    @PutMapping("/pantallas")
    @PreAuthorize("@permisos.puede('perfiles', 'EDITAR')")
    public ResponseEntity<List<Map<String, Object>>> guardarPantallas(
            @RequestBody List<Map<String, Object>> filas) {

        permisos.guardarPantallas(filas);
        return ResponseEntity.ok(permisos.listarPantallas());
    }

    @PutMapping("/{rol}")
    @PreAuthorize("@permisos.puede('perfiles', 'EDITAR')")
    public ResponseEntity<Map<String, Object>> guardar(
            @PathVariable("rol") String rol,
            @RequestBody List<Map<String, Object>> filas) {

        permisos.guardar(rol, filas);
        return ResponseEntity.ok(permisos.matriz());
    }
}
