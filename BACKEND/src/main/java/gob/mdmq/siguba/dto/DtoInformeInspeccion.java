package gob.mdmq.siguba.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Informe de inspeccion tal como lo edita la pantalla y lo leen los
 * generadores de Word y PDF. Las secciones siguen el modelo oficial de la UBA.
 */
@Getter
@Setter
@NoArgsConstructor
public class DtoInformeInspeccion {

    // ── Solo lectura: identifican el tramite ──
    private Long idCodigoInterno;
    private String codigoTramite;
    /** false mientras sea un borrador armado con los datos del tramite. */
    private boolean guardado;
    private LocalDateTime fechaActualizacion;
    private String actualizadoPor;

    // ── 1. Antecedentes ──
    private String documentoAtendido;
    private String tipoInspeccion;
    private LocalDateTime fechaInspeccion;
    private String plagas;
    private String nivel;
    private String asunto;

    // ── 2. Informacion general del lugar ──
    private String direccion;
    private String sectorBarrio;
    private String adminZonal;
    private String tipoLugar;
    private String areasSupervisadas;
    private String coordenadas;
    private String beneficiarios;
    private String personaContactada;
    private List<Participante> participantes = new ArrayList<>();

    // ── 3. Detalle de la inspeccion ──
    private List<DetalleEspecie> detalleEspecies = new ArrayList<>();

    // ── 4. Diagnostico ──
    private String factoresRiesgo;
    private String diagnostico;

    // ── 5. Programa de actuacion ──
    /** ACTIVO o PASIVO. */
    private String tipoControl;
    private String programaActuacion;

    // ── 6 y 7 ──
    private String conclusiones;
    private List<GrupoRecomendacion> recomendaciones = new ArrayList<>();

    // ── 8. Firmas ──
    private String elaboradoNombre;
    private String elaboradoCargo;
    private String revisadoNombre;
    private String revisadoCargo;
    private LocalDate fechaElaboracion;

    // ── 9. Anexo fotografico ──
    private List<Foto> fotos = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Participante {
        private String nombre;
        private String cargo;
        private String institucion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleEspecie {
        private String especie;
        private String nivel;
        private String indicios;
    }

    /** Recomendaciones dirigidas a una entidad (EMASEO EP, Ciudadanía...). */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrupoRecomendacion {
        private String entidad;
        private List<String> items = new ArrayList<>();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Foto {
        /** Ruta dentro del bucket: lo que se guarda. */
        private String ruta;
        /** URL firmada para mostrarla; se ignora al guardar. */
        private String url;
        private String descripcion;
    }
}
