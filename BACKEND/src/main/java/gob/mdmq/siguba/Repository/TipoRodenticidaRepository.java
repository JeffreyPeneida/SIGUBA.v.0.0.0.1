package gob.mdmq.siguba.Repository;

import gob.mdmq.siguba.Entidades.TipoRodenticida;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoRodenticidaRepository extends JpaRepository<TipoRodenticida, Long> {

    @Query("""
        SELECT t
        FROM TipoRodenticida t
        ORDER BY t.nombre
    """)
    List<TipoRodenticida> obtenerTodos();
}
