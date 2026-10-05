package gob.mdmq.siguba.Controller;

import gob.mdmq.siguba.Catalogo.CatalogoService;
import gob.mdmq.siguba.Catalogo.ItemCatalogo;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Catalogos que alimentan los desplegables de los formularios.
 *
 * Todos devuelven la misma forma: {id, nombre} y, si dependen de otro, tambien
 * {idPadre, nombrePadre}. Antes cada uno devolvia la entidad cruda con su
 * propio nombre de clave (idAdminZonal, idParroquia, idTipo...), y la plantilla
 * leia `admin.id`: las opciones valian undefined y ningun desplegable
 * encadenaba con el siguiente.
 *
 * Se apoya en el mismo servicio que la pantalla de administracion, asi que lo
 * que se edita alli aparece aqui sin duplicar consultas.
 */
@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final CatalogoService catalogos;

    public CatalogoController(CatalogoService catalogos) {
        this.catalogos = catalogos;
    }

    // ----------------------------------------------------------- ubicacion

    @GetMapping("/admin-zonal")
    public ResponseEntity<List<ItemCatalogo>> adminZonales() {
        return ResponseEntity.ok(catalogos.listar("admin-zonal", null));
    }

    /**
     * Parroquias de una administracion zonal.
     *
     * Acepta el identificador o el nombre: el desplegable puede haberse poblado
     * de cualquiera de las dos formas segun la pantalla.
     */
    @GetMapping("/parroquia/{zona}")
    public ResponseEntity<List<ItemCatalogo>> parroquias(@PathVariable("zona") String zona) {
        return ResponseEntity.ok(
                catalogos.listar("parroquia", catalogos.resolverId("admin-zonal", zona)));
    }

    @GetMapping("/barrio/{parroquia}")
    public ResponseEntity<List<ItemCatalogo>> barrios(@PathVariable("parroquia") String parroquia) {
        return ResponseEntity.ok(
                catalogos.listar("barrio", catalogos.resolverId("parroquia", parroquia)));
    }

    // ----------------------------------------------------------- denuncia

    @GetMapping("/tipo-denunciante")
    public ResponseEntity<List<ItemCatalogo>> tiposDenunciante() {
        return ResponseEntity.ok(catalogos.listar("tipo-denunciante", null));
    }

    @GetMapping("/dependencia")
    public ResponseEntity<List<ItemCatalogo>> dependencias() {
        return ResponseEntity.ok(catalogos.listar("dependencia", null));
    }

    @GetMapping("/predio")
    public ResponseEntity<List<ItemCatalogo>> predios() {
        return ResponseEntity.ok(catalogos.listar("predio", null));
    }

    @GetMapping("/especie")
    public ResponseEntity<List<ItemCatalogo>> especies() {
        return ResponseEntity.ok(catalogos.listar("especie", null));
    }

    // ---------------------------------------------------------- inspeccion

    @GetMapping("/area-inspeccion")
    public ResponseEntity<List<ItemCatalogo>> areasInspeccion() {
        return ResponseEntity.ok(catalogos.listar("area", null));
    }

    @GetMapping("/motivo-inspeccion")
    public ResponseEntity<List<ItemCatalogo>> motivosInspeccion() {
        return ResponseEntity.ok(catalogos.listar("motivo", null));
    }

    /** Cada especie tiene su propia escala de infestacion. */
    @GetMapping("/nivel-infestacion/{idEspecie}")
    public ResponseEntity<List<ItemCatalogo>> nivelesInfestacion(
            @PathVariable("idEspecie") Long idEspecie) {

        return ResponseEntity.ok(catalogos.listar("nivel-infestacion", idEspecie));
    }

    @GetMapping("/tipo-rodenticida")
    public ResponseEntity<List<ItemCatalogo>> tiposRodenticida() {
        return ResponseEntity.ok(catalogos.listar("tipo-rodenticida", null));
    }

    @GetMapping("/tipo-evidencia")
    public ResponseEntity<List<ItemCatalogo>> tiposEvidencia() {
        return ResponseEntity.ok(catalogos.listar("evidencia", null));
    }
}
