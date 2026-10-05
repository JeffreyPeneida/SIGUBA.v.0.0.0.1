package gob.mdmq.siguba.Repository;

import gob.mdmq.siguba.Entidades.Denunciante;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DenuncianteRepository
        extends JpaRepository<Denunciante, Long> {

    Optional<Denunciante> findByCedula(
            String cedula
    );

}