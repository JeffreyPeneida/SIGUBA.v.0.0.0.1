package gob.mdmq.siguba.Repository;

import gob.mdmq.siguba.Entidades.Predio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PredioRepository extends JpaRepository<Predio, Long> {

    @Query("""
        SELECT p
        FROM Predio p
        ORDER BY p.nombre
    """)
    List<Predio> obtenerPredios();

}