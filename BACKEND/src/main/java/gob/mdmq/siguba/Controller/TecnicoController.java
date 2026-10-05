package gob.mdmq.siguba.Controller;

import org.springframework.security.access.prepost.PreAuthorize;
import gob.mdmq.siguba.Entidades.Tecnico;
import gob.mdmq.siguba.Repository.TecnicoRepository;
import gob.mdmq.siguba.Repository.TramiteRepository;
import gob.mdmq.siguba.dto.DtoTecnicoCarga;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Personal de campo al que se asignan los tramites.
 *
 * Son los usuarios con rol TECNICO, vistos por su ficha de UBA_TECNICO, que es
 * a donde apunta la clave foranea de UBA_TRAMITE. Las dos tablas siguen siendo
 * distintas, pero ya no son dos listas de gente distinta: la ficha se deriva de
 * la cuenta, asi que quien aparece aqui aparece tambien en Usuarios.
 */
@RestController
@RequestMapping("/api/tecnico")
public class TecnicoController {

    private final TecnicoRepository tecnicoRepository;
    private final TramiteRepository tramiteRepository;

    public TecnicoController(TecnicoRepository tecnicoRepository,
                             TramiteRepository tramiteRepository) {
        this.tecnicoRepository = tecnicoRepository;
        this.tramiteRepository = tramiteRepository;
    }

    /**
     * Tecnicos activos con su carga de trabajo.
     *
     * Se devuelven ordenados por pendientes ascendente: quien reparte ve
     * primero a quien esta mas libre, que es la decision que viene a tomar.
     */
    @GetMapping
    @PreAuthorize("@permisos.puedeAlguna('control-plagas:EDITAR', 'tecnicos:VER')")
    public ResponseEntity<List<DtoTecnicoCarga>> conCarga() {

        List<DtoTecnicoCarga> lista = tecnicoRepository.obtenerAsignables()
                .stream()
                .map(this::conSuCarga)
                .sorted(Comparator
                        .comparingLong(DtoTecnicoCarga::getPendientes)
                        .thenComparing(DtoTecnicoCarga::getApellido,
                                Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        return ResponseEntity.ok(lista);
    }

    private DtoTecnicoCarga conSuCarga(Tecnico t) {

        return new DtoTecnicoCarga(
                t.getIdTecnico(),
                t.getNombre(),
                t.getApellido(),
                t.getCedula(),
                t.getCorreo(),
                tramiteRepository.contarPendientesPorTecnico(t.getIdTecnico()),
                tramiteRepository.contarPorTecnico(t.getIdTecnico()));
    }
}
