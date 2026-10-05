package gob.mdmq.siguba.Service;

import gob.mdmq.siguba.Entidades.AdminZonal;
import gob.mdmq.siguba.dto.DtoAdminZonal;
import gob.mdmq.siguba.Repository.AdminZonalRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminZonalService {

    @Autowired
    private AdminZonalRepository adminZonalRepository;

    // GUARDAR
    @Transactional
    public boolean guardarAdminZonal(DtoAdminZonal dtoAdminZonal) {

        boolean respuesta = false;

        try {

            AdminZonal adminZonal = new AdminZonal();

            adminZonal.setIdAdminZonal(dtoAdminZonal.getIdAdminZonal());
            adminZonal.setNombre(dtoAdminZonal.getNombre());

            adminZonalRepository.save(adminZonal);

            respuesta = true;

        } catch (Exception e) {

            e.printStackTrace();

            respuesta = false;
        }

        return respuesta;
    }

    // LISTAR ADMIN ZONALES
    public List<DtoAdminZonal> obtenerAdminZonales() {

        try {

            return adminZonalRepository.obtenerTodos();

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    // LISTAR SOLO NOMBRES
    public List<String> obtenerNombres() {

        try {

            return adminZonalRepository.obtenerNombres();

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

}
