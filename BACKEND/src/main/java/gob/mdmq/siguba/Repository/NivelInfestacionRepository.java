package gob.mdmq.siguba.Repository;

import gob.mdmq.siguba.Entidades.NivelInfestacion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NivelInfestacionRepository extends JpaRepository<NivelInfestacion, Long> {

    /** Los niveles dependen de la especie: cada plaga tiene su propia escala. */
    @Query("""
        SELECT n
        FROM NivelInfestacion n
        WHERE n.especie.idEspecie = :idEspecie
        ORDER BY n.idNivel
    """)
    List<NivelInfestacion> obtenerPorEspecie(@Param("idEspecie") Long idEspecie);
}
