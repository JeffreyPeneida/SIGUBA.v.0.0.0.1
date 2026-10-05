package gob.mdmq.siguba.Repository;

import gob.mdmq.siguba.Entidades.AdminZonal;
import gob.mdmq.siguba.dto.DtoAdminZonal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminZonalRepository extends JpaRepository<AdminZonal, Long> {

    @Query("""
        SELECT new gob.mdmq.siguba.dto.DtoAdminZonal(
            a.idAdminZonal,
            a.nombre
        )
        FROM AdminZonal a
        ORDER BY a.nombre
    """)
    List<DtoAdminZonal> obtenerTodos();

    @Query("""
        SELECT a.nombre
        FROM AdminZonal a
        ORDER BY a.nombre
    """)
    List<String> obtenerNombres();
}