package gob.mdmq.siguba.Repository;

import gob.mdmq.siguba.Entidades.TipoDenunciante;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoDenuncianteRepository
        extends JpaRepository<TipoDenunciante, Long> {

    @Query("""
        SELECT t
        FROM TipoDenunciante t
        ORDER BY t.nombre
    """)
    List<TipoDenunciante> obtenerTipos();

}