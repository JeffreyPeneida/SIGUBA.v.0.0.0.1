package gob.mdmq.siguba.Controller;

import org.springframework.security.access.prepost.PreAuthorize;
import gob.mdmq.siguba.dto.DtoAdminZonal;
import gob.mdmq.siguba.Service.AdminZonalService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/adminzonal")
public class AdminZonalController {

    @Autowired
    private AdminZonalService adminZonalService;

    // LISTAR ADMINISTRACIONES ZONALES
    @GetMapping("/buscarAdministracionesZonales")
    public ResponseEntity<List<DtoAdminZonal>> buscarAdministracionesZonales() {

        try {

            return ResponseEntity.ok(
                    adminZonalService.obtenerAdminZonales());

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Error al consultar Administraciones Zonales",
                    e);
        }
    }

    // LISTAR SOLO NOMBRES
    @GetMapping("/buscarNombresAdministracionesZonales")
    public ResponseEntity<List<String>> buscarNombresAdministracionesZonales() {

        try {

            return ResponseEntity.ok(
                    adminZonalService.obtenerNombres());

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Error al consultar nombres de Administraciones Zonales",
                    e);
        }
    }

    // CREAR ADMINISTRACION ZONAL
    @PostMapping("/crearAdministracionZonal")
    @PreAuthorize("@permisos.puede('catalogos', 'CREAR')")
    public ResponseEntity<String> crearAdministracionZonal(
            @RequestBody DtoAdminZonal dtoAdminZonal) {

        try {

            boolean respuesta =
                    adminZonalService.guardarAdminZonal(dtoAdminZonal);

            String mensaje;

            if (respuesta) {

                mensaje = "Administración Zonal registrada correctamente";

            } else {

                mensaje = "Se produjo un error al registrar la Administración Zonal";
            }

            return new ResponseEntity<>(
                    mensaje,
                    HttpStatus.CREATED);

        } catch (Exception e) {

            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

}



