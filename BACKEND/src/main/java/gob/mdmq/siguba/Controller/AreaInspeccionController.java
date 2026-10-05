package gob.mdmq.siguba.Controller;

import org.springframework.security.access.prepost.PreAuthorize;
import gob.mdmq.siguba.dto.DtoAreaInspeccion;
import gob.mdmq.siguba.dto.DtoResponse;
import gob.mdmq.siguba.Service.AreaInspeccionService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/areainspeccion")
@CrossOrigin(origins = "*")
public class AreaInspeccionController {

    @Autowired
    private AreaInspeccionService AreaInspeccionservice;

    @GetMapping("/buscarAreasInspeccion")
    public ResponseEntity<List<DtoAreaInspeccion>>
            buscarAreasInspeccion() {

        try {

            return ResponseEntity.ok(
                    AreaInspeccionservice
                            .buscarAreasInspeccion());

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Error al consultar áreas de inspección",
                    e);
        }
    }

    @GetMapping("/buscarAreaInspeccion/{idArea}")
    public ResponseEntity<DtoAreaInspeccion>
            buscarAreaInspeccionPorId(
                    @PathVariable("idArea")
                    Long idArea) {

        try {

            return ResponseEntity.ok(
                    AreaInspeccionservice
                            .buscarAreaInspeccionPorId(
                                    idArea));

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Error al consultar área de inspección",
                    e);
        }
    }

    @GetMapping("/buscarAreaInspeccionNombre/{nombre}")
    public ResponseEntity<List<DtoAreaInspeccion>>
            buscarAreaInspeccionPorNombre(
                    @PathVariable("nombre")
                    String nombre) {

        try {

            return ResponseEntity.ok(
                    AreaInspeccionservice
                            .buscarAreaInspeccionPorNombre(
                                    nombre));

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Error al consultar área de inspección",
                    e);
        }
    }

    @PostMapping("/crearAreaInspeccion")
    @PreAuthorize("@permisos.puede('catalogos', 'CREAR')")
    public ResponseEntity<?> crearAreaInspeccion(
            @RequestBody
            DtoAreaInspeccion dtoAreaInspeccion) {

        try {

            boolean respuesta =
                    AreaInspeccionservice
                            .guardarAreaInspeccion(
                                    dtoAreaInspeccion);

            String mensaje;

            if (respuesta) {

                mensaje =
                        "Área de inspección registrada correctamente";

            } else {

                mensaje =
                        "Error al registrar área de inspección";
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

}