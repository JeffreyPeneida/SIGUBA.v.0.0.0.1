package gob.mdmq.siguba.Repository;

import gob.mdmq.siguba.Entidades.Parroquia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ParroquiaRepository
        extends JpaRepository<Parroquia, Long> {

    @Query("""
        SELECT p
        FROM Parroquia p
        WHERE p.adminZonal.nombre = :nombre
        ORDER BY p.nombre
    """)
    List<Parroquia> obtenerPorZona(
            @Param("nombre")
            String nombre
    );


    // El desplegable puede enviar el identificador en vez del nombre.
    @Query("""
        SELECT p
        FROM Parroquia p
        WHERE p.adminZonal.idAdminZonal = :id
        ORDER BY p.nombre
    """)
    List<Parroquia> obtenerPorZonaId(@Param("id") Long id);

}
