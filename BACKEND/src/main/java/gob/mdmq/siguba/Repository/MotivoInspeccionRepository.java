package gob.mdmq.siguba.Repository;

import gob.mdmq.siguba.Entidades.MotivoInspeccion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MotivoInspeccionRepository extends JpaRepository<MotivoInspeccion, Long> {

    @Query("""
        SELECT m
        FROM MotivoInspeccion m
        ORDER BY m.nombre
    """)
    List<MotivoInspeccion> obtenerTodos();
}
