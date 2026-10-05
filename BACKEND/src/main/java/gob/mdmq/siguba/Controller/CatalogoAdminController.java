package gob.mdmq.siguba.Controller;

import gob.mdmq.siguba.Catalogo.CatalogoService;
import gob.mdmq.siguba.Catalogo.ItemCatalogo;
import gob.mdmq.siguba.dto.DtoResponse;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Administracion de catalogos.
 *
 * Leer es para cualquier usuario con sesion, porque los formularios consumen
 * estos mismos datos. Crear, editar y borrar es solo de administracion.
 */
@RestController
@RequestMapping("/api/catalogos")
public class CatalogoAdminController {

    private final CatalogoService catalogoService;

    public CatalogoAdminController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    /** Que catalogos existen y como se relacionan; lo usa la pantalla de administracion. */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> definiciones() {
        return ResponseEntity.ok(catalogoService.definiciones());
    }

    /** Filas de un catalogo. Con `padre` se acotan las de un padre concreto. */
    @GetMapping("/{clave}")
    public ResponseEntity<List<ItemCatalogo>> listar(
            @PathVariable("clave") String clave,
            @RequestParam(value = "padre", required = false) Long idPadre) {

        return ResponseEntity.ok(catalogoService.listar(clave, idPadre));
    }

    @PostMapping("/{clave}")
    @PreAuthorize("@permisos.puede('catalogos', 'CREAR')")
    public ResponseEntity<ItemCatalogo> crear(
            @PathVariable("clave") String clave,
            @RequestBody ItemCatalogo datos) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogoService.crear(clave, datos));
    }

    @PutMapping("/{clave}/{id}")
    @PreAuthorize("@permisos.puede('catalogos', 'EDITAR')")
    public ResponseEntity<ItemCatalogo> actualizar(
            @PathVariable("clave") String clave,
            @PathVariable("id") Long id,
            @RequestBody ItemCatalogo datos) {

        return ResponseEntity.ok(catalogoService.actualizar(clave, id, datos));
    }

    @DeleteMapping("/{clave}/{id}")
    @PreAuthorize("@permisos.puede('catalogos', 'ELIMINAR')")
    public ResponseEntity<DtoResponse> borrar(
            @PathVariable("clave") String clave,
            @PathVariable("id") Long id) {

        catalogoService.borrar(clave, id);

        return ResponseEntity.ok(new DtoResponse(
                HttpStatus.OK.value(), "Registro eliminado"));
    }
}
