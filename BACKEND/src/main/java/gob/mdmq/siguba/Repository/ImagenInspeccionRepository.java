package gob.mdmq.siguba.Repository;

import gob.mdmq.siguba.Entidades.ImagenInspeccion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ImagenInspeccionRepository extends JpaRepository<ImagenInspeccion, Long> {

    // Las fotos anteriores a ORDEN quedan al final, en el orden en que se subieron.
    @Query("""
        SELECT i
        FROM ImagenInspeccion i
        WHERE i.tramite.idCodigoInterno = :idCodigoInterno
        ORDER BY CASE WHEN i.orden IS NULL THEN 1 ELSE 0 END, i.orden, i.idImagen
    """)
    List<ImagenInspeccion> obtenerPorTramite(@Param("idCodigoInterno") Long idCodigoInterno);

    @Modifying
    @Query("DELETE FROM ImagenInspeccion i WHERE i.tramite.idCodigoInterno = :idCodigoInterno")
    void borrarPorTramite(@Param("idCodigoInterno") Long idCodigoInterno);
}
