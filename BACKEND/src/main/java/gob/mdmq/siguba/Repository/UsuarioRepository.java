package gob.mdmq.siguba.Repository;

import gob.mdmq.siguba.Entidades.RolUsuario;
import gob.mdmq.siguba.Entidades.Usuario;
import gob.mdmq.siguba.dto.DtoUsuario;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // ==================================================
    // INSERTAR
    // ==================================================
    default Usuario guardar(Usuario usuario) {
        return save(usuario);
    }

    // ==================================================
    // BUSCAR POR USUARIO
    // ==================================================
    @Query("""
        SELECT u
        FROM Usuario u
        WHERE u.usuario = :usuario
        AND u.estado = 'ACTIVO'
    """)
    Optional<Usuario> buscarPorUsuario(
            @Param("usuario") String usuario
    );

    // ==================================================
    // BUSCAR POR CEDULA
    // ==================================================
    @Query("""
        SELECT u
        FROM Usuario u
        WHERE u.cedula = :cedula
        AND u.estado = 'ACTIVO'
    """)
    Optional<Usuario> buscarPorCedula(
            @Param("cedula") String cedula
    );

    // ==================================================
    // LOGIN
    // ==================================================
    @Query("""
        SELECT u
        FROM Usuario u
        WHERE u.usuario = :usuario
        AND u.password = :password
        AND u.estado = 'ACTIVO'
    """)
    Optional<Usuario> login(
            @Param("usuario") String usuario,
            @Param("password") String password
    );

    // ==================================================
    // BUSCAR POR ROL
    // ==================================================
    @Query("""
        SELECT u
        FROM Usuario u
        WHERE u.rol = :rol
        AND u.estado = 'ACTIVO'
    """)
    List<Usuario> buscarPorRol(
            @Param("rol") RolUsuario rol
    );

    // ==================================================
    // BUSQUEDA GENERAL
    // ==================================================
    @Query("""
        SELECT u
        FROM Usuario u
        WHERE u.estado = 'ACTIVO'
        AND (
            UPPER(u.nombre)
            LIKE UPPER(CONCAT('%', :texto, '%'))
            OR
            UPPER(u.apellido)
            LIKE UPPER(CONCAT('%', :texto, '%'))
            OR
            UPPER(u.cedula)
            LIKE UPPER(CONCAT('%', :texto, '%'))
        )
    """)
    List<Usuario> buscar(
            @Param("texto") String texto
    );

    // ==================================================
    // OBTENER TODOS
    // ==================================================
    @Query("""
        SELECT u
        FROM Usuario u
        WHERE u.estado = 'ACTIVO'
    """)
    List<Usuario> obtenerUsuarios();

    /** Activos e inactivos, para que la administracion pueda reactivar cuentas. */
    @Query("SELECT u FROM Usuario u ORDER BY u.estado, u.apellido, u.nombre")
    List<Usuario> obtenerTodos();

    // ==================================================
    // ACTUALIZAR
    // ==================================================
    @Transactional
    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
        UPDATE Usuario u
        SET
            u.nombre=:#{#u.nombre},
            u.apellido=:#{#u.apellido},
            u.cedula=:#{#u.cedula},
            u.fechaNacimiento=:#{#u.fechaNacimiento},
            u.usuario=:#{#u.usuario},
            u.mail=:#{#u.mail},
            u.adminZonal=:#{#u.adminZonal},
            u.password=:#{#u.password},
            u.rol=:#{#u.rol}
        WHERE u.idUsuario=:id
        AND u.estado='ACTIVO'
    """)
    int actualizarUsuario(
            @Param("id") Integer id,
            @Param("u") Usuario u
    );

    // ==================================================
    // ELIMINAR (SOFT DELETE)
    // ==================================================
    @Transactional
    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
        UPDATE Usuario u
        SET u.estado='INACTIVO'
        WHERE u.cedula=:cedula
        AND u.estado='ACTIVO'
    """)
    int eliminarUsuario(
            @Param("cedula") String cedula
    );

    // ==================================================
    // RESTAURAR USUARIO
    // ==================================================
    @Transactional
    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
        UPDATE Usuario u
        SET u.estado='ACTIVO'
        WHERE u.cedula=:cedula
        AND u.estado='INACTIVO'
    """)
    int restaurarUsuario(
            @Param("cedula") String cedula
    );

    // ==================================================
    // VALIDACIONES
    // ==================================================
    boolean existsByCedulaAndEstado(
            String cedula,
            String estado
    );

    boolean existsByUsuarioAndEstado(
            String usuario,
            String estado
    );

    boolean existsByMailAndEstado(
            String mail,
            String estado
    );

    // ==================================================
    // OBTENER TODOS DTO
    // ==================================================
    @Query("""
    SELECT NEW gob.mdmq.siguba.dto.DtoUsuario(
        u.idUsuario,
        u.nombre,
        u.apellido,
        u.cedula,
        u.fechaNacimiento,
        u.usuario,
        u.mail,
        u.adminZonal,
        u.rol,
        u.estado
    )
    FROM Usuario u
    WHERE u.estado = 'ACTIVO'
    """)
    List<DtoUsuario> obtenerUsuariosDTO();

    // ==================================================
    // BUSCAR POR CEDULA DTO
    // ==================================================
    @Query("""
    SELECT NEW gob.mdmq.siguba.dto.DtoUsuario(
        u.idUsuario,
        u.nombre,
        u.apellido,
        u.cedula,
        u.fechaNacimiento,
        u.usuario,
        u.mail,
        u.adminZonal,
        u.rol,
        u.estado
    )
    FROM Usuario u
    WHERE u.cedula = :cedula
    AND u.estado = 'ACTIVO'
    """)
    DtoUsuario buscarPorCedulaDTO(
            @Param("cedula") String cedula
    );

    // ==================================================
    // BUSCAR POR USUARIO DTO
    // ==================================================
    @Query("""
    SELECT NEW gob.mdmq.siguba.dto.DtoUsuario(
        u.idUsuario,
        u.nombre,
        u.apellido,
        u.cedula,
        u.fechaNacimiento,
        u.usuario,
        u.mail,
        u.adminZonal,
        u.rol,
        u.estado
    )
    FROM Usuario u
    WHERE u.usuario = :usuario
    AND u.estado = 'ACTIVO'
    """)
    DtoUsuario buscarPorUsuarioDTO(
            @Param("usuario") String usuario
    );

    // ==================================================
    // LOGIN DTO
    // ==================================================
    @Query("""
    SELECT NEW gob.mdmq.siguba.dto.DtoUsuario(
        u.idUsuario,
        u.nombre,
        u.apellido,
        u.cedula,
        u.fechaNacimiento,
        u.usuario,
        u.mail,
        u.adminZonal,
        u.rol,
        u.estado
    )
    FROM Usuario u
    WHERE u.usuario = :usuario
    AND u.password = :password
    AND u.estado = 'ACTIVO'
    """)
    DtoUsuario loginDTO(
            @Param("usuario") String usuario,
            @Param("password") String password
    );

    // ==================================================
    // BUSCAR POR ROL DTO
    // ==================================================
    @Query("""
    SELECT NEW gob.mdmq.siguba.dto.DtoUsuario(
        u.idUsuario,
        u.nombre,
        u.apellido,
        u.cedula,
        u.fechaNacimiento,
        u.usuario,
        u.mail,
        u.adminZonal,
        u.rol,
        u.estado
    )
    FROM Usuario u
    WHERE u.rol = :rol
    AND u.estado = 'ACTIVO'
    """)
    List<DtoUsuario> buscarPorRolDTO(
            @Param("rol") RolUsuario rol
    );

    // ==================================================
    // BUSQUEDA GENERAL DTO
    // ==================================================
    @Query("""
    SELECT NEW gob.mdmq.siguba.dto.DtoUsuario(
        u.idUsuario,
        u.nombre,
        u.apellido,
        u.cedula,
        u.fechaNacimiento,
        u.usuario,
        u.mail,
        u.adminZonal,
        u.rol,
        u.estado
    )
    FROM Usuario u
    WHERE u.estado = 'ACTIVO'
    AND (
        UPPER(u.nombre)
        LIKE UPPER(CONCAT('%', :texto, '%'))
        OR
        UPPER(u.apellido)
        LIKE UPPER(CONCAT('%', :texto, '%'))
        OR
        UPPER(u.cedula)
        LIKE UPPER(CONCAT('%', :texto, '%'))
    )
    """)
    List<DtoUsuario> buscarDTO(
            @Param("texto") String texto
    );

}
