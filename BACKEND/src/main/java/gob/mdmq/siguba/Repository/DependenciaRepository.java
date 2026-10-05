package gob.mdmq.siguba.Repository;

import gob.mdmq.siguba.Entidades.Dependencia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DependenciaRepository extends JpaRepository<Dependencia, Long> {

    @Query("""
        SELECT d
        FROM Dependencia d
        ORDER BY d.nombre
    """)
    List<Dependencia> obtenerDependencias();

}
