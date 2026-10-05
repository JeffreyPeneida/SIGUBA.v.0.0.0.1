package gob.mdmq.siguba.Repository;

import gob.mdmq.siguba.Entidades.AreaInspeccion;
import gob.mdmq.siguba.dto.DtoAreaInspeccion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AreaInspeccionRepository extends JpaRepository<AreaInspeccion, Long> {

    @Query("""
        SELECT NEW gob.mdmq.siguba.dto.DtoAreaInspeccion(
            a.idArea,
            a.nombre
        )
        FROM AreaInspeccion a
        ORDER BY a.nombre
    """)
    List<DtoAreaInspeccion> buscarAreasInspeccion();

    @Query("""
        SELECT NEW gob.mdmq.siguba.dto.DtoAreaInspeccion(
            a.idArea,
            a.nombre
        )
        FROM AreaInspeccion a
        WHERE a.idArea = :idArea
    """)
    DtoAreaInspeccion buscarAreaInspeccionPorId(
            @Param("idArea") Long idArea);

    @Query("""
        SELECT NEW gob.mdmq.siguba.dto.DtoAreaInspeccion(
            a.idArea,
            a.nombre
        )
        FROM AreaInspeccion a
        WHERE UPPER(a.nombre)
        LIKE UPPER(CONCAT('%', :nombre, '%'))
        ORDER BY a.nombre
    """)
    List<DtoAreaInspeccion> buscarAreaInspeccionPorNombre(
            @Param("nombre") String nombre);

}