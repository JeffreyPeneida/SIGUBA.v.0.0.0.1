package gob.mdmq.siguba.Repository;

import gob.mdmq.siguba.Entidades.TipoEvidencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoEvidenciaRepository extends JpaRepository<TipoEvidencia, Long> {

    @Query("""
        SELECT e
        FROM TipoEvidencia e
        ORDER BY e.nombre
    """)
    List<TipoEvidencia> obtenerTodos();
}
