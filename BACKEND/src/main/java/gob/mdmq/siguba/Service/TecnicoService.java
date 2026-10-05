package gob.mdmq.siguba.Service;

import gob.mdmq.siguba.Entidades.RolUsuario;
import gob.mdmq.siguba.Entidades.Tecnico;
import gob.mdmq.siguba.Entidades.Usuario;
import gob.mdmq.siguba.Repository.TecnicoRepository;
import gob.mdmq.siguba.Repository.UsuarioRepository;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mantiene la ficha de UBA_TECNICO alineada con la cuenta de UBA_USUARIO.
 *
 * Un tecnico es un usuario con rol TECNICO. La tabla UBA_TECNICO sigue
 * existiendo porque es a donde apunta la clave foranea de UBA_TRAMITE, pero ya
 * no se administra por separado: se deriva de la cuenta. Asi no vuelven a
 * aparecer tecnicos que no estan en Usuarios ni usuarios a los que no se les
 * puede asignar nada.
 */
@Service
public class TecnicoService {

    private final TecnicoRepository tecnicoRepository;
    private final UsuarioRepository usuarioRepository;

    public TecnicoService(TecnicoRepository tecnicoRepository,
                          UsuarioRepository usuarioRepository) {
        this.tecnicoRepository = tecnicoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Crea o actualiza la ficha del usuario si su rol es TECNICO.
     *
     * No borra la ficha cuando alguien deja de ser tecnico: los tramites que ya
     * atendio siguen apuntando a ella. Deja de listarse porque la consulta de
     * asignables exige cuenta activa con rol TECNICO.
     */
    @Transactional
    public void sincronizar(Usuario usuario) {

        if (usuario == null
                || usuario.getIdUsuario() == null
                || usuario.getRol() != RolUsuario.TECNICO) {
            return;
        }

        Tecnico tecnico = tecnicoRepository
                .findByIdUsuario(usuario.getIdUsuario())
                .orElseGet(Tecnico::new);

        tecnico.setIdUsuario(usuario.getIdUsuario());
        tecnico.setNombre(usuario.getNombre());
        tecnico.setApellido(usuario.getApellido());
        tecnico.setCedula(usuario.getCedula());
        tecnico.setTecnicoUsuario(usuario.getUsuario());
        tecnico.setCorreo(usuario.getMail());
        tecnico.setEstado(usuario.getEstado() == null ? "ACTIVO" : usuario.getEstado());

        // La clave vive solo en UBA_USUARIO: aqui se deja vacia a proposito.
        tecnico.setPassword(null);

        tecnicoRepository.save(tecnico);
    }

    /** Igual que el anterior, pero partiendo del nombre de usuario. */
    @Transactional
    public void sincronizarPorUsuario(String nombreUsuario) {
        usuarioRepository.buscarPorUsuario(nombreUsuario).ifPresent(this::sincronizar);
    }

    /** Ficha de tecnico de la cuenta que tiene la sesion abierta. */
    public Optional<Tecnico> deUsuario(String nombreUsuario) {
        return usuarioRepository.buscarPorUsuario(nombreUsuario)
                .flatMap(u -> tecnicoRepository.findByIdUsuario(u.getIdUsuario()));
    }
}
