package gob.mdmq.siguba.Service;

import gob.mdmq.siguba.Entidades.AreaInspeccion;
import gob.mdmq.siguba.dto.DtoAreaInspeccion;
import gob.mdmq.siguba.Repository.AreaInspeccionRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AreaInspeccionService {

    @Autowired
    private AreaInspeccionRepository areaInspeccionRepository;

    @Transactional
    public boolean guardarAreaInspeccion(
            DtoAreaInspeccion dtoAreaInspeccion) {

        boolean respuesta = false;

        try {

            AreaInspeccion areaInspeccion =
                    new AreaInspeccion();

            areaInspeccion.setIdArea(
                    dtoAreaInspeccion.getIdArea());

            areaInspeccion.setNombre(
                    dtoAreaInspeccion.getNombre());

            areaInspeccionRepository.save(
                    areaInspeccion);

            respuesta = true;

        } catch (Exception e) {

            e.printStackTrace();

            respuesta = false;
        }

        return respuesta;
    }

    public List<DtoAreaInspeccion> buscarAreasInspeccion() {

        try {

            return areaInspeccionRepository
                    .buscarAreasInspeccion();

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    public DtoAreaInspeccion buscarAreaInspeccionPorId(
            Long idArea) {

        try {

            return areaInspeccionRepository
                    .buscarAreaInspeccionPorId(idArea);

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    public List<DtoAreaInspeccion> buscarAreaInspeccionPorNombre(
            String nombre) {

        try {

            return areaInspeccionRepository
                    .buscarAreaInspeccionPorNombre(nombre);

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

}