package gob.mdmq.siguba.Repository;

import gob.mdmq.siguba.Entidades.Barrio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BarrioRepository
        extends JpaRepository<Barrio, Long> {

    @Query("""
        SELECT b
        FROM Barrio b
        WHERE b.parroquia.nombre = :nombre
        ORDER BY b.nombre
    """)
    List<Barrio> obtenerPorParroquia(
            @Param("nombre")
            String nombre
    );


    @Query("""
        SELECT b
        FROM Barrio b
        WHERE b.parroquia.idParroquia = :id
        ORDER BY b.nombre
    """)
    List<Barrio> obtenerPorParroquiaId(@Param("id") Long id);

}
