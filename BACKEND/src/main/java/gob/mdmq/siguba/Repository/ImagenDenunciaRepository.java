package gob.mdmq.siguba.Repository;

import gob.mdmq.siguba.Entidades.ImagenDenuncia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ImagenDenunciaRepository extends JpaRepository<ImagenDenuncia, Long> {

    @Query("""
        SELECT i
        FROM ImagenDenuncia i
        WHERE i.tramite.idCodigoInterno = :idCodigoInterno
        ORDER BY i.idImagen
    """)
    List<ImagenDenuncia> obtenerPorTramite(@Param("idCodigoInterno") Long idCodigoInterno);
}
