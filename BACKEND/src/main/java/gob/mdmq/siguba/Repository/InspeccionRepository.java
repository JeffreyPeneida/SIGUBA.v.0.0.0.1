package gob.mdmq.siguba.Repository;

import gob.mdmq.siguba.Entidades.*;

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
public interface InspeccionRepository extends JpaRepository<Inspeccion, Long> {

    // ==================================================
    // 1. TRAER TRÁMITE COMPLETO (DENUNCIA + UBICACIÓN + TODO)
    // ==================================================
    @Query("""
        SELECT t
        FROM Tramite t
        LEFT JOIN FETCH t.denunciante
        LEFT JOIN FETCH t.tecnico
        LEFT JOIN FETCH t.ubicacion u
        LEFT JOIN FETCH u.adminZonal
        LEFT JOIN FETCH u.parroquia
        LEFT JOIN FETCH u.barrio
        LEFT JOIN FETCH u.predio
        LEFT JOIN FETCH t.areaInspeccion
        LEFT JOIN FETCH t.motivoInspeccion
        WHERE t.idCodigoInterno = :id
    """)
    Optional<Tramite> obtenerTramiteCompleto(
            @Param("id") Long id
    );

    // ==================================================
    // 2. OBTENER ESPECIES DE UN TRÁMITE
    // ==================================================
    @Query("""
        SELECT t.especies
        FROM Tramite t
        WHERE t.idCodigoInterno = :id
    """)
    List<Especie> obtenerEspeciesPorTramite(
            @Param("id") Long id
    );

    // ==================================================
    // 3. TRÁMITES-ESPECIE (control directo de relación)
    // ==================================================
    @Query("""
        SELECT t.especies
        FROM Tramite t
        WHERE t.idCodigoInterno = :id
    """)
    List<Especie> listarEspeciesTramite(
            @Param("id") Long id
    );

    // ==================================================
    // 4. IMÁGENES DE INSPECCIÓN
    // ==================================================
    @Query("""
        SELECT i
        FROM ImagenInspeccion i
        WHERE i.tramite.idCodigoInterno = :id
        ORDER BY i.idImagen
    """)
    List<ImagenInspeccion> listarImagenesInspeccion(
            @Param("id") Long id
    );

    // ==================================================
    // 5. IMÁGENES DE UBICACIÓN (MAPA)
    // ==================================================
    @Query("""
        SELECT ui
        FROM UbicacionImagen ui
        WHERE ui.ubicacion.idUbicacion = :idUbicacion
    """)
    List<UbicacionImagen> listarImagenesUbicacion(
            @Param("idUbicacion") Long idUbicacion
    );

    // ==================================================
    // 6. CARGAR NIVEL DE INFESTACIÓN POR ESPECIE
    // ==================================================
    @Query("""
        SELECT n
        FROM NivelInfestacion n
        WHERE n.especie.idEspecie = :idEspecie
    """)
    List<NivelInfestacion> listarNivelesPorEspecie(
            @Param("idEspecie") Long idEspecie
    );

    // ==================================================
    // 7. INSPECCIÓN (TABLA INDEPENDIENTE)
    // ==================================================
    @Query("""
        SELECT i
        FROM Inspeccion i
        WHERE i.especie.idEspecie = :idEspecie
    """)
    List<Inspeccion> listarInspeccionesPorEspecie(
            @Param("idEspecie") Long idEspecie
    );

    // ==================================================
    // 8. GUARDAR INSPECCIÓN
    // ==================================================
    @Transactional
    @Override
    Inspeccion save(Inspeccion inspeccion);

    // ==================================================
    // 9. ACTUALIZAR TRÁMITE (INSPECCIÓN GENERAL)
    // ==================================================
    @Transactional
    @Modifying
    @Query("""
        UPDATE Tramite t
        SET
            t.fechaInspeccion = :fecha,
            t.horaInspeccion = :hora,
            t.areaInspeccion.idArea = :idArea,
            t.motivoInspeccion.idMotivo = :idMotivo,
            t.descripcion = :descripcion,
            t.conclusiones = :conclusiones,
            t.recomendaciones = :recomendaciones,
            t.usuario.idUsuario = :idUsuario
        WHERE t.idCodigoInterno = :id
    """)
    int actualizarTramiteInspeccion(
            @Param("fecha") Date fecha,
            @Param("hora") Date hora,
            @Param("idArea") Long idArea,
            @Param("idMotivo") Long idMotivo,
            @Param("descripcion") String descripcion,
            @Param("conclusiones") String conclusiones,
            @Param("recomendaciones") String recomendaciones,
            @Param("idUsuario") Long idUsuario,
            @Param("id") Long idCodigoInterno
    );

    // ==================================================
    // 10. INSERTAR USO RODENTICIDA
    // ==================================================
    @Transactional
    @Modifying
    @Query("""
        INSERT INTO UsoRodenticida (tramite, tipoRodenticida, cantidad)
        VALUES (:tramite, :tipo, :cantidad)
    """)
    void insertarUsoRodenticida(
            @Param("tramite") Tramite tramite,
            @Param("tipo") TipoRodenticida tipo,
            @Param("cantidad") Integer cantidad
    );

    // ==================================================
    // 11. INSERTAR EVIDENCIA INSPECCIÓN
    // ==================================================
    @Transactional
    @Modifying
    @Query("""
        INSERT INTO EvidenciaInspeccion (inspeccion, tipoEvidencia, descripcionOtro)
        VALUES (:inspeccion, :tipo, :desc)
    """)
    void insertarEvidencia(
            @Param("inspeccion") Inspeccion inspeccion,
            @Param("tipo") TipoEvidencia tipo,
            @Param("desc") String descripcionOtro
    );

    // ==================================================
    // 12. INSERTAR NIVEL INSPECCIÓN
    // ==================================================
    @Transactional
    @Modifying
    @Query("""
        INSERT INTO NivelInfestacion (especie, nombre, descripcion)
        VALUES (:especie, :nombre, :descripcion)
    """)
    void insertarNivelInfestacion(
            @Param("especie") Especie especie,
            @Param("nombre") String nombre,
            @Param("descripcion") String descripcion
    );

    // ==================================================
    // 15. TRAER INSPECCIONES COMPLETAS
    // ==================================================
    @Query("""
        SELECT i
        FROM Inspeccion i
        JOIN FETCH i.especie
        LEFT JOIN FETCH i.nivelInfestacion
    """)
    List<Inspeccion> listarInspeccionesCompletas();

}