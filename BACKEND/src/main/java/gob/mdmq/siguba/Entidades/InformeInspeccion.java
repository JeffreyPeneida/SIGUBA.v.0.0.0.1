package gob.mdmq.siguba.Entidades;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Informe tecnico de una inspeccion, uno por tramite (misma clave).
 *
 * PARTICIPANTES, DETALLE_ESPECIES y RECOMENDACIONES guardan JSON; quien
 * los interpreta es InformeInspeccionService.
 */
@Entity
@Table(name = "UBA_INFORME_INSPECCION", schema = "PROYECTO_UBA")
@Getter
@Setter
public class InformeInspeccion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_CODIGO_INTERNO")
    private Long idCodigoInterno;

    @Column(name = "DOCUMENTO_ATENDIDO", length = 300)
    private String documentoAtendido;

    @Column(name = "TIPO_INSPECCION", length = 300)
    private String tipoInspeccion;

    @Column(name = "FECHA_INSPECCION")
    private LocalDateTime fechaInspeccion;

    @Column(name = "PLAGAS", length = 300)
    private String plagas;

    @Column(name = "NIVEL", length = 50)
    private String nivel;

    @Column(name = "ASUNTO", length = 300)
    private String asunto;

    @Column(name = "DIRECCION", length = 500)
    private String direccion;

    @Column(name = "SECTOR_BARRIO", length = 200)
    private String sectorBarrio;

    @Column(name = "ADMIN_ZONAL", length = 150)
    private String adminZonal;

    @Column(name = "TIPO_LUGAR", length = 200)
    private String tipoLugar;

    @Column(name = "AREAS_SUPERVISADAS", length = 500)
    private String areasSupervisadas;

    @Column(name = "COORDENADAS", length = 100)
    private String coordenadas;

    @Column(name = "BENEFICIARIOS", length = 50)
    private String beneficiarios;

    @Column(name = "PERSONA_CONTACTADA", length = 200)
    private String personaContactada;

    @Column(name = "PARTICIPANTES")
    private String participantes;

    @Column(name = "DETALLE_ESPECIES")
    private String detalleEspecies;

    @Column(name = "FACTORES_RIESGO", length = 1000)
    private String factoresRiesgo;

    @Column(name = "DIAGNOSTICO")
    private String diagnostico;

    /** ACTIVO o PASIVO. */
    @Column(name = "TIPO_CONTROL", length = 10)
    private String tipoControl;

    @Column(name = "PROGRAMA_ACTUACION")
    private String programaActuacion;

    @Column(name = "CONCLUSIONES")
    private String conclusiones;

    @Column(name = "RECOMENDACIONES")
    private String recomendaciones;

    @Column(name = "ELABORADO_NOMBRE", length = 200)
    private String elaboradoNombre;

    @Column(name = "ELABORADO_CARGO", length = 200)
    private String elaboradoCargo;

    @Column(name = "REVISADO_NOMBRE", length = 200)
    private String revisadoNombre;

    @Column(name = "REVISADO_CARGO", length = 200)
    private String revisadoCargo;

    @Column(name = "FECHA_ELABORACION")
    private LocalDate fechaElaboracion;

    @Column(name = "FECHA_ACTUALIZACION")
    private LocalDateTime fechaActualizacion;

    @Column(name = "ACTUALIZADO_POR", length = 50)
    private String actualizadoPor;
}
