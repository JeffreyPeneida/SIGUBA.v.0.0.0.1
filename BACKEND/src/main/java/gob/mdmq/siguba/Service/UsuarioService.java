package gob.mdmq.siguba.Service;

import gob.mdmq.siguba.Entidades.RolUsuario;
import gob.mdmq.siguba.Entidades.Usuario;
import gob.mdmq.siguba.dto.DtoUsuario;
import gob.mdmq.siguba.Repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

import gob.mdmq.siguba.Security.JwtService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    // Un usuario con rol TECNICO necesita ademas su ficha en UBA_TECNICO, que
    // es a donde apunta la clave foranea de los tramites.
    @Autowired
    private TecnicoService tecnicoService;

    // ==================================================
    // GUARDAR
    // ==================================================
    @Transactional
    public boolean guardarUsuario(DtoUsuario dtoUsuario) {

        boolean respuesta = false;

        try {

            Usuario usuario = convertirEntidad(dtoUsuario);

            tecnicoService.sincronizar(usuarioRepository.save(usuario));

            respuesta = true;

        } catch (Exception e) {

            e.printStackTrace();

            respuesta = false;
        }

        return respuesta;
    }

    /** Token para un usuario ya autenticado. */
    public String generarToken(String usuario) {

        return usuarioRepository.buscarPorUsuario(usuario)
                .map(jwtService::generar)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el usuario " + usuario));
    }

    // ==================================================
    // OBTENER TODOS
    // ==================================================
    public List<DtoUsuario> obtenerUsuarios() {
        return obtenerUsuarios(false);
    }

    public List<DtoUsuario> obtenerUsuarios(boolean incluirInactivos) {

        try {

            return (incluirInactivos
                        ? usuarioRepository.obtenerTodos()
                        : usuarioRepository.obtenerUsuarios())
                    .stream()
                    .map(this::convertirDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    // ==================================================
    // BUSCAR POR CEDULA
    // ==================================================
    public DtoUsuario obtenerPorCedula(String cedula) {

        try {

            Optional<Usuario> usuario
                    = usuarioRepository.buscarPorCedula(cedula);

            return usuario
                    .map(this::convertirDto)
                    .orElse(null);

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    // ==================================================
    // BUSCAR POR USUARIO
    // ==================================================
    public DtoUsuario obtenerPorUsuario(String usuario) {

        try {

            Optional<Usuario> resultado
                    = usuarioRepository.buscarPorUsuario(usuario);

            return resultado
                    .map(this::convertirDto)
                    .orElse(null);

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    // ==================================================
    // LOGIN
    // ==================================================
    public DtoUsuario login(
            String usuario,
            String password) {

        try {

            // La comparacion ya no puede hacerse en SQL: BCrypt genera un hash
            // distinto cada vez, asi que hay que verificarlo en Java.
            return usuarioRepository.buscarPorUsuario(usuario)
                    .filter(u -> u.getPassword() != null
                              && passwordEncoder.matches(password, u.getPassword()))
                    .map(this::convertirDto)
                    .orElse(null);

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    // ==================================================
    // BUSCAR POR ROL
    // ==================================================
    public List<DtoUsuario> buscarPorRol(
            RolUsuario rol) {

        try {

            return usuarioRepository.buscarPorRol(rol)
                    .stream()
                    .map(this::convertirDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    // ==================================================
    // BUSQUEDA GENERAL
    // ==================================================
    public List<DtoUsuario> buscar(String texto) {

        try {

            return usuarioRepository.buscar(texto)
                    .stream()
                    .map(this::convertirDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    // ==================================================
    // ACTUALIZAR
    // ==================================================
    @Transactional
    public boolean actualizarUsuario(
            Integer id,
            DtoUsuario dtoUsuario) {

        boolean respuesta = false;

        try {

            Usuario usuario
                    = convertirEntidad(dtoUsuario);

            int resultado
                    = usuarioRepository.actualizarUsuario(
                            id,
                            usuario);

            respuesta = resultado > 0;

            // Puede haber cambiado el rol, el nombre o el correo: la ficha de
            // tecnico se rehace desde la cuenta ya actualizada.
            if (respuesta) {
                usuarioRepository.findById(id).ifPresent(tecnicoService::sincronizar);
            }

        } catch (Exception e) {

            e.printStackTrace();

            respuesta = false;
        }

        return respuesta;
    }

    // ==================================================
    // ELIMINAR
    // ==================================================
    @Transactional
    public boolean eliminarUsuario(String cedula) {

        boolean respuesta = false;

        try {

            int resultado
                    = usuarioRepository.eliminarUsuario(cedula);

            respuesta = resultado > 0;

        } catch (Exception e) {

            e.printStackTrace();

            respuesta = false;
        }

        return respuesta;
    }

    // ==================================================
    // RESTAURAR
    // ==================================================
    @Transactional
    public boolean restaurarUsuario(String cedula) {

        boolean respuesta = false;

        try {

            int resultado
                    = usuarioRepository.restaurarUsuario(cedula);

            respuesta = resultado > 0;

        } catch (Exception e) {

            e.printStackTrace();

            respuesta = false;
        }

        return respuesta;
    }

    // ==================================================
    // VALIDACIONES
    // ==================================================
    public boolean existeCedula(String cedula) {

        try {

            return usuarioRepository
                    .existsByCedulaAndEstado(
                            cedula,
                            "ACTIVO");

        } catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }

    public boolean existeUsuario(String usuario) {

        try {

            return usuarioRepository
                    .existsByUsuarioAndEstado(
                            usuario,
                            "ACTIVO");

        } catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }

    public boolean existeMail(String mail) {

        try {

            return usuarioRepository
                    .existsByMailAndEstado(
                            mail,
                            "ACTIVO");

        } catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }

    // ==================================================
    // CONVERSIÓN ENTIDAD -> DTO
    // ==================================================
    private DtoUsuario convertirDto(Usuario u) {

        DtoUsuario dto = new DtoUsuario();

        dto.setIdUsuario(u.getIdUsuario());
        dto.setNombre(u.getNombre());
        dto.setApellido(u.getApellido());
        dto.setCedula(u.getCedula());
        dto.setFechaNacimiento(u.getFechaNacimiento());
        dto.setUsuario(u.getUsuario());
        dto.setMail(u.getMail());
        dto.setAdminZonal(u.getAdminZonal());
        dto.setRol(u.getRol());
        dto.setTipoIdentificacion(u.getTipoIdentificacion());
        dto.setDenominacion(u.getDenominacion());
        dto.setMailInstitucional(u.getMailInstitucional());
        dto.setDireccion(u.getDireccion());
        dto.setTelefono(u.getTelefono());
        dto.setMovil(u.getMovil());
        dto.setAceptacionMedios(u.getAceptacionMedios());
        dto.setSim(u.getSim());
        dto.setSsoId(u.getSsoId());
        dto.setTipoUsuario(u.getTipoUsuario());
        dto.setEstado(u.getEstado());

        return dto;
    }

    // ==================================================
    // CONVERSIÓN DTO -> ENTIDAD
    // ==================================================
    private Usuario convertirEntidad(DtoUsuario dto) {

        Usuario usuario = new Usuario();

        usuario.setIdUsuario(dto.getIdUsuario());
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setCedula(dto.getCedula());
        usuario.setFechaNacimiento(dto.getFechaNacimiento());
        usuario.setUsuario(dto.getUsuario());
        usuario.setMail(dto.getMail());
        usuario.setAdminZonal(dto.getAdminZonal());
        usuario.setRol(dto.getRol());
        usuario.setEstado(dto.getEstado());

        // Sin esto el usuario se guardaba sin clave y nunca podia iniciar sesion.
        // La contrasena se cifra aqui: nunca se persiste en texto plano.
        usuario.setPassword(
                dto.getPassword() == null || dto.getPassword().isBlank()
                        ? null
                        : passwordEncoder.encode(dto.getPassword()));

        usuario.setDenominacion(dto.getDenominacion());
        usuario.setMailInstitucional(dto.getMailInstitucional());
        usuario.setDireccion(dto.getDireccion());
        usuario.setTelefono(dto.getTelefono());
        usuario.setMovil(dto.getMovil());
        usuario.setAceptacionMedios(dto.getAceptacionMedios());
        usuario.setSim(dto.getSim());

        // ssoId, tipoIdentificacion y tipoUsuario son @Column(nullable = false)
        // heredados del sistema con Keycloak, que aqui no esta activo. Nadie los
        // rellenaba, asi que Hibernate abortaba todo alta de usuario con
        // PropertyValueException antes siquiera de llegar a la base.
        usuario.setSsoId(
                dto.getSsoId() != null ? dto.getSsoId() : "");
        usuario.setTipoIdentificacion(
                dto.getTipoIdentificacion() != null ? dto.getTipoIdentificacion() : "CEDULA");
        usuario.setTipoUsuario(
                dto.getTipoUsuario() != null ? dto.getTipoUsuario() : 1L);

        return usuario;
    }
}
