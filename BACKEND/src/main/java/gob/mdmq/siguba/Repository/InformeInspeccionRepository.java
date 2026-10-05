package gob.mdmq.siguba.Repository;

import gob.mdmq.siguba.Entidades.InformeInspeccion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InformeInspeccionRepository extends JpaRepository<InformeInspeccion, Long> {
}
