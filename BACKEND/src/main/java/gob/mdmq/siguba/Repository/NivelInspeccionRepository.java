package gob.mdmq.siguba.Repository;

import gob.mdmq.siguba.Entidades.NivelInspeccion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NivelInspeccionRepository extends JpaRepository<NivelInspeccion, Long> {

    /**
     * Su clave foranea al tramite es NO_ACTION, asi que hay que vaciar estas
     * filas antes de borrar el tramite; las demas tablas hijas cascadean solas.
     */
    @Modifying
    @Query("""
        DELETE FROM NivelInspeccion n
        WHERE n.tramite.idCodigoInterno = :idCodigoInterno
    """)
    int borrarPorTramite(@Param("idCodigoInterno") Long idCodigoInterno);
}
