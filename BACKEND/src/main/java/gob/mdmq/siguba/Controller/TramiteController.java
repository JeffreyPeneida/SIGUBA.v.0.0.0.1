package gob.mdmq.siguba.Controller;

import gob.mdmq.siguba.Entidades.Tramite;
import gob.mdmq.siguba.Repository.UsuarioRepository;
import gob.mdmq.siguba.Service.TecnicoService;
import gob.mdmq.siguba.Service.TramiteService;
import gob.mdmq.siguba.Service.LimpiezaService;
import org.springframework.security.access.prepost.PreAuthorize;
import gob.mdmq.siguba.dto.DtoResponse;
import gob.mdmq.siguba.dto.DtoTramite;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import gob.mdmq.siguba.dto.DtoActualizarInspeccion;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/tramite")
public class TramiteController {

    @Autowired
    private TramiteService tramiteService;

    @Autowired
    private LimpiezaService limpiezaService;

    @Autowired
    private TecnicoService tecnicoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/buscarTramite/{idTramite}")
    @PreAuthorize("@accesoTramite.puedeVerCodigo(#idTramite)")
    public ResponseEntity<Optional<Tramite>> buscarTramite(
            @PathVariable("idTramite") String idTramite) {

        try {

            return ResponseEntity.ok(
                    tramiteService.buscarPorCodigo(idTramite));

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Error al consultar trámite",
                    e);
        }
    }

    @GetMapping("/buscarTramiteInterno/{idCodigoInterno}")
    @PreAuthorize("@accesoTramite.puedeVer(#idCodigoInterno)")
    public ResponseEntity<Optional<Tramite>> buscarTramiteInterno(
            @PathVariable("idCodigoInterno") Long idCodigoInterno) {

        try {

            return ResponseEntity.ok(
                    tramiteService.buscarPorIdInterno(idCodigoInterno));

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Error al consultar trámite",
                    e);
        }
    }

    @GetMapping("/buscarTramites")
    @PreAuthorize("@permisos.puede('control-plagas', 'VER')")
    public ResponseEntity<List<Tramite>> buscarTramites() {

        try {

            return ResponseEntity.ok(
                    tramiteService.obtenerTramites());

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Error al consultar trámites",
                    e);
        }
    }

    @GetMapping("/buscarDenuncias")
    @PreAuthorize("@permisos.puede('control-plagas', 'VER')")
    public ResponseEntity<List<Tramite>> buscarDenuncias() {

        try {

            return ResponseEntity.ok(
                    tramiteService.obtenerDenuncias());

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Error al consultar denuncias",
                    e);
        }
    }

    @GetMapping("/buscarInspecciones")
    @PreAuthorize("@permisos.puede('control-plagas', 'VER')")
    public ResponseEntity<List<Tramite>> buscarInspecciones() {

        try {

            return ResponseEntity.ok(
                    tramiteService.obtenerInspecciones());

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Error al consultar inspecciones",
                    e);
        }
    }

    @GetMapping("/buscarPorCedula/{cedula}")
    @PreAuthorize("@permisos.puede('control-plagas', 'VER')")
    public ResponseEntity<List<Tramite>> buscarPorCedula(
            @PathVariable("cedula") String cedula) {

        try {

            return ResponseEntity.ok(
                    tramiteService.buscarPorCedula(cedula));

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Error al consultar trámites por cédula",
                    e);
        }
    }

    @GetMapping("/buscarPorTecnico/{idTecnico}")
    @PreAuthorize("@permisos.puede('control-plagas', 'VER')")
    public ResponseEntity<List<Tramite>> buscarPorTecnico(
            @PathVariable("idTecnico") Long idTecnico) {

        try {

            return ResponseEntity.ok(
                    tramiteService.obtenerPorTecnico(idTecnico));

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Error al consultar trámites por técnico",
                    e);
        }
    }

    /**
     * Lo asignado a quien tiene la sesion abierta.
     *
     * El identificador de tecnico no puede venir del navegador: la sesion trae
     * el usuario, y es el backend quien resuelve su ficha. Antes el frontend se
     * traia todos los tramites asignados y filtraba a la vista, de modo que un
     * tecnico veia el trabajo de los demas.
     */
    @GetMapping("/misAsignados")
    @PreAuthorize("@permisos.puede('inspecciones', 'VER')")
    public ResponseEntity<List<Tramite>> misAsignados(Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return tecnicoService.deUsuario(principal.getName())
                .map(t -> ResponseEntity.ok(
                        tramiteService.obtenerPorTecnico(t.getIdTecnico())))
                .orElseGet(() -> ResponseEntity.ok(List.of()));
    }

    /**
     * Denuncias de quien tiene la sesion abierta: las que registro y las hechas
     * con su cedula.
     *
     * El usuario sale del token, no de la URL. Antes la pantalla "Mis
     * denuncias" pedia /buscarTramites y cualquier usuario veia las denuncias
     * de todos.
     */
    @GetMapping("/misDenuncias")
    @PreAuthorize("@permisos.puede('mis-denuncias', 'VER')")
    public ResponseEntity<List<Tramite>> misDenuncias(Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return usuarioRepository.buscarPorUsuario(principal.getName())
                .map(u -> ResponseEntity.ok(
                        tramiteService.buscarDeUsuario(u.getIdUsuario(), u.getCedula())))
                .orElseGet(() -> ResponseEntity.ok(List.of()));
    }

    @GetMapping("/buscarSinTecnico")
    @PreAuthorize("@permisos.puede('control-plagas', 'VER')")
    public ResponseEntity<List<Tramite>> buscarSinTecnico() {

        try {

            return ResponseEntity.ok(
                    tramiteService.obtenerSinTecnico());

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Error al consultar trámites sin técnico",
                    e);
        }
    }

    @PostMapping("/crearTramite")
    @PreAuthorize("@permisos.puede('registrar-denuncia', 'CREAR')")
    public ResponseEntity<?> crearTramite(
            @RequestBody DtoTramite dtoTramite) {

        try {

            boolean respuesta;
            String mensaje;

            try {

                respuesta = tramiteService.guardarTramite(dtoTramite);

                if (respuesta) {
                    mensaje = "Se ha registrado correctamente el trámite";
                } else {
                    mensaje = "Se ha producido un error al registrar el trámite";
                }

            } catch (Exception e) {

                mensaje = "Se ha producido un error al registrar el trámite";
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

    @GetMapping("/obtenerTecnicoAsignado/{idCodigoInterno}")
    @PreAuthorize("@accesoTramite.puedeVer(#idCodigoInterno)")
    public ResponseEntity<Long> obtenerTecnicoAsignado(
            @PathVariable Long idCodigoInterno) {

        try {

            return ResponseEntity.ok(
                    tramiteService.obtenerTecnicoAsignado(
                            idCodigoInterno));

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No se pudo obtener el técnico",
                    e);
        }
    }

    @GetMapping("/obtenerCodigoTramite/{idCodigoInterno}")
    @PreAuthorize("@accesoTramite.puedeVer(#idCodigoInterno)")
    public ResponseEntity<String> obtenerCodigoTramite(
            @PathVariable Long idCodigoInterno) {

        try {

            return ResponseEntity.ok(
                    tramiteService.obtenerCodigoTramite(
                            idCodigoInterno));

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No se pudo obtener el código",
                    e);
        }
    }

    @PutMapping("/actualizarInspeccion")
    @PreAuthorize("@permisos.puede('inspecciones', 'CREAR')")
    public ResponseEntity<DtoResponse> actualizarInspeccion(
            @RequestBody DtoActualizarInspeccion dto) {

        try {

            boolean respuesta
                    = tramiteService.actualizarInspeccion(
                            dto.getFechaInspeccion(),
                            dto.getHoraInspeccion(),
                            dto.getIdArea(),
                            dto.getIdMotivo(),
                            dto.getDescripcion(),
                            dto.getConclusiones(),
                            dto.getRecomendaciones(),
                            dto.getIdRegistra(),
                            dto.getIdCodigoInterno());

            return ResponseEntity.ok(
                    new DtoResponse(
                            HttpStatus.OK.value(),
                            respuesta
                                    ? "Inspección actualizada correctamente"
                                    : "No fue posible actualizar la inspección"));

        } catch (Exception e) {

            return new ResponseEntity<>(
                    new DtoResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/asignarTecnico")
    @PreAuthorize("@permisos.puede('control-plagas', 'EDITAR')")
    public ResponseEntity<DtoResponse> asignarTecnico(
            @RequestParam Long idTecnico,
            @RequestParam Long idInterno) {

        try {

            boolean respuesta
                    = tramiteService.asignarTecnico(
                            idTecnico,
                            idInterno);

            return ResponseEntity.ok(
                    new DtoResponse(
                            HttpStatus.OK.value(),
                            respuesta
                                    ? "Técnico asignado correctamente"
                                    : "No fue posible asignar el técnico"));

        } catch (Exception e) {

            return new ResponseEntity<>(
                    new DtoResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/actualizarUbicacion")
    @PreAuthorize("@permisos.puede('control-plagas', 'EDITAR')")
    public ResponseEntity<DtoResponse> actualizarUbicacion(
            @RequestParam Long idInterno,
            @RequestParam BigDecimal latitud,
            @RequestParam BigDecimal longitud) {

        try {

            boolean respuesta
                    = tramiteService.actualizarUbicacion(
                            idInterno,
                            latitud,
                            longitud);

            return ResponseEntity.ok(
                    new DtoResponse(
                            HttpStatus.OK.value(),
                            respuesta
                                    ? "Ubicación actualizada correctamente"
                                    : "No fue posible actualizar la ubicación"));

        } catch (Exception e) {

            return new ResponseEntity<>(
                    new DtoResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/eliminarTramite/{idInterno}")
    @PreAuthorize("@permisos.puede('control-plagas', 'ELIMINAR')")
    public ResponseEntity<DtoResponse> eliminarTramite(
            @PathVariable Long idInterno) {

        try {

            boolean respuesta
                    = tramiteService.eliminar(idInterno);

            return ResponseEntity.ok(
                    new DtoResponse(
                            HttpStatus.OK.value(),
                            respuesta
                                    ? "Trámite eliminado correctamente"
                                    : "No fue posible eliminar el trámite"));

        } catch (Exception e) {

            return new ResponseEntity<>(
                    new DtoResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/restaurarTramite/{idInterno}")
    @PreAuthorize("@permisos.puede('control-plagas', 'ELIMINAR')")
    public ResponseEntity<DtoResponse> restaurarTramite(
            @PathVariable Long idInterno) {

        try {

            boolean respuesta
                    = tramiteService.restaurar(idInterno);

            return ResponseEntity.ok(
                    new DtoResponse(
                            HttpStatus.OK.value(),
                            respuesta
                                    ? "Trámite restaurado correctamente"
                                    : "No fue posible restaurar el trámite"));

        } catch (Exception e) {

            return new ResponseEntity<>(
                    new DtoResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Borrado definitivo. No es lo mismo que eliminarTramite, que solo marca el
     * estado como INACTIVO y se puede deshacer: esto no tiene vuelta atras, y
     * ademas borra las fotos de MinIO. Reservado a la administracion.
     */
    @DeleteMapping("/borrar/{idInterno}")
    @PreAuthorize("@permisos.puede('control-plagas', 'ELIMINAR')")
    public ResponseEntity<DtoResponse> borrarDefinitivo(@PathVariable Long idInterno) {

        limpiezaService.borrarTramite(idInterno);

        return ResponseEntity.ok(new DtoResponse(
                HttpStatus.OK.value(), "Trámite borrado definitivamente"));
    }
}



