package gob.mdmq.siguba.Repository;

import gob.mdmq.siguba.Entidades.Tramite;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TramiteRepository
        extends JpaRepository<Tramite, Long> {

    // ==================================================
    // INSERTAR
    // ==================================================
    default Tramite guardar(Tramite tramite) {

        if (tramite.getEstado() == null) {
            tramite.setEstado("ACTIVO");
        }

        return save(tramite);
    }

    // ==================================================
    // GENERAR CÓDIGO (MAX ID INTERNO)
    // ==================================================
    @Query("""
        SELECT COALESCE(MAX(t.idCodigoInterno),0)
        FROM Tramite t
    """)
    Long obtenerUltimoId();

    // ==================================================
    // OBTENER DENUNCIAS
    // ==================================================
    @Query("""
        SELECT t
        FROM Tramite t
        LEFT JOIN FETCH t.denunciante
        LEFT JOIN FETCH t.tecnico
        LEFT JOIN FETCH t.ubicacion
        LEFT JOIN FETCH t.barrio
        LEFT JOIN FETCH t.areaInspeccion
        LEFT JOIN FETCH t.motivoInspeccion
        WHERE t.estado='ACTIVO'
        AND t.fechaInspeccion IS NULL
        ORDER BY t.fechaDenuncia DESC
    """)
    List<Tramite> obtenerDenuncias();

    // ==================================================
    // OBTENER INSPECCIONES
    // ==================================================
    @Query("""
        SELECT t
        FROM Tramite t
        LEFT JOIN FETCH t.denunciante
        LEFT JOIN FETCH t.tecnico
        LEFT JOIN FETCH t.ubicacion
        LEFT JOIN FETCH t.barrio
        LEFT JOIN FETCH t.areaInspeccion
        LEFT JOIN FETCH t.motivoInspeccion
        WHERE t.estado='ACTIVO'
        AND t.fechaInspeccion IS NOT NULL
        ORDER BY t.fechaDenuncia DESC
    """)
    List<Tramite> obtenerInspecciones();

    // ==================================================
    // OBTENER TODOS
    // ==================================================
    @Query("""
        SELECT t
        FROM Tramite t
        LEFT JOIN FETCH t.denunciante
        LEFT JOIN FETCH t.tecnico
        LEFT JOIN FETCH t.ubicacion
        LEFT JOIN FETCH t.barrio
        LEFT JOIN FETCH t.areaInspeccion
        LEFT JOIN FETCH t.motivoInspeccion
        WHERE t.estado='ACTIVO'
        ORDER BY t.fechaDenuncia DESC
    """)
    List<Tramite> obtenerTramites();

    // ==================================================
    // BUSCAR POR CÓDIGO TRÁMITE
    // ==================================================
    @Query("""
        SELECT t
        FROM Tramite t
        WHERE t.idTramite=:codigo
        AND t.estado='ACTIVO'
    """)
    Optional<Tramite> buscarPorCodigo(
            @Param("codigo") String codigo
    );

    // ==================================================
    // BUSCAR POR ID TRÁMITE
    // ==================================================
    Optional<Tramite> findByIdTramite(String idTramite);

    // ==================================================
    // BUSCAR POR ID INTERNO
    // ==================================================
    Optional<Tramite> findByIdCodigoInterno(Long idInterno);

    // ==================================================
    // BUSCAR POR CÓDIGO INTERNO (INSPECCIÓN)
    // ==================================================
    @Query("""
        SELECT t
        FROM Tramite t
        WHERE t.idCodigoInterno = :idCodigoInterno
    """)
    Optional<Tramite> buscarPorCodigoInterno(
            @Param("idCodigoInterno") Long idCodigoInterno);

    // ==================================================
    // OBTENER TÉCNICO ASIGNADO
    // ==================================================
    @Query("""
        SELECT t.tecnico.idTecnico
        FROM Tramite t
        WHERE t.idCodigoInterno = :idCodigoInterno
    """)
    Long obtenerTecnicoAsignado(
            @Param("idCodigoInterno") Long idCodigoInterno);

    // ==================================================
    // OBTENER CÓDIGO DE TRÁMITE
    // ==================================================
    @Query("""
        SELECT t.idTramite
        FROM Tramite t
        WHERE t.idCodigoInterno = :idCodigoInterno
    """)
    String obtenerCodigoTramite(
            @Param("idCodigoInterno") Long idCodigoInterno);

    // ==================================================
    // ACTUALIZAR INSPECCIÓN
    // ==================================================
    @Transactional
    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
        UPDATE Tramite t
        SET
            t.fechaInspeccion = :fechaInspeccion,
            t.horaInspeccion = :horaInspeccion,
            t.areaInspeccion.idArea = :idArea,
            t.motivoInspeccion.idMotivo = :idMotivo,
            t.descripcion = :descripcion,
            t.conclusiones = :conclusiones,
            t.recomendaciones = :recomendaciones,
            t.usuario.idUsuario = :idRegistra
        WHERE t.idCodigoInterno = :idCodigoInterno
    """)
    int actualizarInspeccion(
            @Param("fechaInspeccion") Date fechaInspeccion,
            @Param("horaInspeccion") Date horaInspeccion,
            @Param("idArea") Long idArea,
            @Param("idMotivo") Long idMotivo,
            @Param("descripcion") String descripcion,
            @Param("conclusiones") String conclusiones,
            @Param("recomendaciones") String recomendaciones,
            @Param("idRegistra") Long idRegistra,
            @Param("idCodigoInterno") Long idCodigoInterno
    );

    // ==================================================
    // BUSCAR POR CÉDULA DENUNCIANTE
    // ==================================================
    @Query("""
        SELECT t
        FROM Tramite t
        JOIN t.denunciante d
        WHERE d.cedula=:cedula
        AND t.estado='ACTIVO'
        ORDER BY t.fechaDenuncia DESC
    """)
    List<Tramite> buscarPorCedula(
            @Param("cedula") String cedula
    );

    // ==================================================
    // DENUNCIAS DE UN USUARIO
    // Las que registro con su cuenta y las hechas a su nombre (su cedula).
    // ==================================================
    @Query("""
        SELECT t
        FROM Tramite t
        LEFT JOIN t.denunciante d
        WHERE (t.usuario.idUsuario=:idUsuario OR d.cedula=:cedula)
        AND t.estado='ACTIVO'
        ORDER BY t.fechaDenuncia DESC
    """)
    List<Tramite> buscarDeUsuario(
            @Param("idUsuario") Integer idUsuario,
            @Param("cedula") String cedula
    );

    /** True si el tramite lo registro ese usuario o se hizo con su cedula. */
    @Query("""
        SELECT COUNT(t) > 0
        FROM Tramite t
        LEFT JOIN t.denunciante d
        WHERE t.idCodigoInterno=:idCodigoInterno
        AND (t.usuario.idUsuario=:idUsuario OR d.cedula=:cedula)
    """)
    boolean esDeUsuario(
            @Param("idCodigoInterno") Long idCodigoInterno,
            @Param("idUsuario") Integer idUsuario,
            @Param("cedula") String cedula
    );

    @Query("SELECT t.idCodigoInterno FROM Tramite t WHERE t.idTramite=:codigo")
    Optional<Long> buscarIdInternoPorCodigo(@Param("codigo") String codigo);


    // ==================================================
    // OBTENER POR TÉCNICO
    // ==================================================
    @Query("""
        SELECT t
        FROM Tramite t
        WHERE t.tecnico.idTecnico=:idTecnico
        AND t.estado='ACTIVO'
        ORDER BY t.fechaDenuncia DESC
    """)
    List<Tramite> obtenerPorTecnico(
            @Param("idTecnico") Long idTecnico
    );

    // ==================================================
    // OBTENER SIN TÉCNICO
    // ==================================================
    @Query("""
        SELECT t
        FROM Tramite t
        WHERE t.tecnico IS NULL
        AND t.estado='ACTIVO'
        ORDER BY t.fechaDenuncia DESC
    """)
    List<Tramite> obtenerSinTecnico();

    // ==================================================
    // ASIGNAR TÉCNICO
    // ==================================================
    @Transactional
    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
        UPDATE Tramite t
        SET
            t.tecnico.idTecnico=:idTecnico,
            t.fechaAsignacionTecnico=CURRENT_TIMESTAMP
        WHERE t.idCodigoInterno=:idInterno
    """)
    int asignarTecnico(
            @Param("idTecnico") Long idTecnico,
            @Param("idInterno") Long idInterno
    );

    // ==================================================
    // ACTUALIZAR UBICACIÓN
    // ==================================================
    @Transactional
    @Modifying
    @Query("""
        UPDATE Tramite t
        SET
            t.latitud=:latitud,
            t.longitud=:longitud
        WHERE t.idCodigoInterno=:id
    """)
    int actualizarUbicacion(
            @Param("id") Long id,
            @Param("latitud") BigDecimal latitud,
            @Param("longitud") BigDecimal longitud
    );

    // ==================================================
    // ACTUALIZAR CÓDIGO
    // ==================================================
    @Transactional
    @Modifying
    @Query("""
        UPDATE Tramite t
        SET t.idTramite=:codigo
        WHERE t.idCodigoInterno=:id
    """)
    int actualizarCodigo(
            @Param("id") Long id,
            @Param("codigo") String codigo
    );

    // ==================================================
    // ELIMINAR (SOFT DELETE)
    // ==================================================
    @Transactional
    @Modifying
    @Query("""
        UPDATE Tramite t
        SET t.estado='INACTIVO'
        WHERE t.idCodigoInterno=:id
    """)
    int eliminar(
            @Param("id") Long id
    );

    // ==================================================
    // RESTAURAR
    // ==================================================
    @Transactional
    @Modifying
    @Query("""
        UPDATE Tramite t
        SET t.estado='ACTIVO'
        WHERE t.idCodigoInterno=:id
    """)
    int restaurar(
            @Param("id") Long id
    );

    // ==================================================
    // VALIDACIONES
    // ==================================================
    boolean existsByIdTramite(String idTramite);

    boolean existsByIdCodigoInterno(Long idCodigoInterno);


    // ------------------------------------------------------------------
    // CARGA DE TRABAJO POR TECNICO
    // ------------------------------------------------------------------

    /** Tramites asignados a un tecnico que aun no tienen inspeccion. */
    @Query("""
        SELECT COUNT(t)
        FROM Tramite t
        WHERE t.tecnico.idTecnico = :idTecnico
        AND t.fechaInspeccion IS NULL
        AND t.estado = 'ACTIVO'
    """)
    long contarPendientesPorTecnico(@Param("idTecnico") Long idTecnico);

    /** Total asignado historicamente, para ver el reparto acumulado. */
    @Query("""
        SELECT COUNT(t)
        FROM Tramite t
        WHERE t.tecnico.idTecnico = :idTecnico
        AND t.estado = 'ACTIVO'
    """)
    long contarPorTecnico(@Param("idTecnico") Long idTecnico);

}
