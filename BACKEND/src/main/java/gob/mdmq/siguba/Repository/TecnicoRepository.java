package gob.mdmq.siguba.Repository;

import gob.mdmq.siguba.Entidades.Tecnico;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {

    // ==================================================
    // INSERTAR TECNICO
    // Equivalente:
    // dao.crearTecnico(nuevoTecnico)
    // ==================================================
    default Tecnico guardar(Tecnico tecnico) {
        return save(tecnico);
    }

    // ==================================================
    // BUSCAR TECNICO POR CEDULA
    // Usado en:
    // dao.buscarPorCedula(...)
    // btnBuscar
    // btnEditTec
    // actualizarTecnico
    // ==================================================
    @Query(
            "SELECT t "
            + "FROM Tecnico t "
            + "WHERE t.cedula = :cedula "
            + "AND t.estado='ACTIVO'"
    )
    Optional<Tecnico> buscarPorCedula(
            @Param("cedula") String cedula
    );

    // ==================================================
    // BUSCAR POR USUARIO
    // Util.generarUsuarioTec()
    // ==================================================
    @Query(
            "SELECT t "
            + "FROM Tecnico t "
            + "WHERE t.tecnicoUsuario = :usuario "
            + "AND t.estado='ACTIVO'"
    )
    Optional<Tecnico> buscarPorUsuario(
            @Param("usuario") String usuario
    );

    // ==================================================
    // VALIDAR CORREO
    // ==================================================
    @Query(
            "SELECT t "
            + "FROM Tecnico t "
            + "WHERE upper(t.correo)=upper(:mail) "
            + "AND t.estado='ACTIVO'"
    )
    Optional<Tecnico> buscarPorMail(
            @Param("mail") String mail
    );

    // ==================================================
    // LOGIN TECNICO
    // ==================================================
    @Query(
            "SELECT t "
            + "FROM Tecnico t "
            + "WHERE t.tecnicoUsuario=:usuario "
            + "AND t.password=:password "
            + "AND t.estado='ACTIVO'"
    )
    Optional<Tecnico> login(
            @Param("usuario") String usuario,
            @Param("password") String password
    );

    // ==================================================
    // OBTENER TODOS ACTIVOS
    // ==================================================
    @Query(
            "SELECT t "
            + "FROM Tecnico t "
            + "WHERE t.estado='ACTIVO' "
            + "ORDER BY t.nombre"
    )
    List<Tecnico> obtenerTecnicos();

    // ==================================================
    // TECNICOS ASIGNABLES
    //
    // Un tecnico solo puede recibir trabajo si tiene cuenta activa con rol
    // TECNICO: sin ella no podria entrar a atenderlo. Por eso la lista sale del
    // cruce de las dos tablas y no de UBA_TECNICO a secas, que era lo que hacia
    // aparecer al asignar a gente que no existia como usuario.
    // ==================================================
    @Query(
            "SELECT t "
            + "FROM Tecnico t "
            + "JOIN Usuario u ON u.idUsuario = t.idUsuario "
            + "WHERE u.estado = 'ACTIVO' "
            + "AND u.rol = gob.mdmq.siguba.Entidades.RolUsuario.TECNICO "
            + "ORDER BY u.apellido, u.nombre"
    )
    List<Tecnico> obtenerAsignables();

    // ==================================================
    // FICHA DE TECNICO DE UNA CUENTA
    // Para que la bandeja del tecnico filtre por su identificador real.
    // ==================================================
    Optional<Tecnico> findByIdUsuario(Integer idUsuario);

    // ==================================================
    // BUSQUEDA GENERAL
    // ==================================================
    @Query(
            "SELECT t "
            + "FROM Tecnico t "
            + "WHERE t.estado='ACTIVO' "
            + "AND ("
            + "upper(t.nombre) LIKE upper(concat('%',:texto,'%')) "
            + "OR upper(t.apellido) LIKE upper(concat('%',:texto,'%')) "
            + "OR upper(t.cedula) LIKE upper(concat('%',:texto,'%')) "
            + "OR upper(t.tecnicoUsuario) LIKE upper(concat('%',:texto,'%'))"
            + ")"
    )
    List<Tecnico> buscar(
            @Param("texto") String texto
    );

    // ==================================================
    // ACTUALIZAR TECNICO
    // Equivalente:
    // dao.actualizarTecnico(...)
    // ==================================================
    @Transactional
    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query(
            "UPDATE Tecnico t SET "
            + "t.nombre=:#{#tec.nombre}, "
            + "t.apellido=:#{#tec.apellido}, "
            + "t.cedula=:#{#tec.cedula}, "
            + "t.tecnicoUsuario=:#{#tec.tecnicoUsuario}, "
            + "t.correo=:#{#tec.correo}, "
            + "t.password=:#{#tec.password} "
            + "WHERE t.idTecnico=:id "
            + "AND t.estado='ACTIVO'"
    )
    int actualizarTecnico(
            @Param("id") Integer id,
            @Param("tec") Tecnico tecnico
    );

    // ==================================================
    // ELIMINAR (BAJA LOGICA)
    // Equivalente:
    // dao.eliminarTecnico(...)
    // ==================================================
    @Transactional
    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query(
            "UPDATE Tecnico t "
            + "SET t.estado='INACTIVO' "
            + "WHERE t.cedula=:cedula "
            + "AND t.estado='ACTIVO'"
    )
    int eliminarTecnico(
            @Param("cedula") String cedula
    );

    // ==================================================
    // VALIDACIONES
    // ==================================================
    boolean existsByCedulaAndEstado(
            String cedula,
            String estado
    );

    boolean existsByTecnicoUsuarioAndEstado(
            String usuario,
            String estado
    );

    boolean existsByCorreoAndEstado(
            String mail,
            String estado
    );

}