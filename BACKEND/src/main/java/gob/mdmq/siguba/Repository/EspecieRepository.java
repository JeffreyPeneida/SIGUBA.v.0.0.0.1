package gob.mdmq.siguba.Repository;

import gob.mdmq.siguba.Entidades.Especie;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EspecieRepository extends JpaRepository<Especie, Long> {

    @Query("""
        SELECT e
        FROM Especie e
        ORDER BY e.nombre
    """)
    List<Especie> obtenerTodos();
}
