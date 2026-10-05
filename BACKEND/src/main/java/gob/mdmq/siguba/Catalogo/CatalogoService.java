package gob.mdmq.siguba.Catalogo;

import gob.mdmq.siguba.Exception.RecursoNoEncontradoException;
import gob.mdmq.siguba.Exception.ReglaNegocioException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD generico de catalogos.
 *
 * Se usa SQL directo y no JPA a proposito: los 13 catalogos comparten forma y
 * con entidades harian falta 13 repositorios y 13 servicios casi identicos.
 *
 * Los nombres de tabla y columna vienen SIEMPRE de RegistroCatalogos, que es
 * una lista cerrada en codigo; lo unico que llega del exterior son valores, y
 * esos van como parametros. No hay concatenacion de datos del usuario.
 */
@Service
public class CatalogoService {

    private final NamedParameterJdbcTemplate jdbc;
    private final RegistroCatalogos registro;

    public CatalogoService(NamedParameterJdbcTemplate jdbc, RegistroCatalogos registro) {
        this.jdbc = jdbc;
        this.registro = registro;
    }

    // ------------------------------------------------------------------ leer

    public List<ItemCatalogo> listar(String clave, Long idPadre) {

        DefinicionCatalogo d = registro.buscar(clave);

        StringBuilder sql = new StringBuilder("SELECT c.")
                .append(d.columnaId()).append(" AS id, c.")
                .append(d.columnaNombre()).append(" AS nombre");

        if (d.tieneDescripcion()) {
            sql.append(", c.").append(d.columnaDescripcion()).append(" AS descripcion");
        }

        if (d.tienePadre()) {
            DefinicionCatalogo p = registro.buscar(d.clavePadre());
            sql.append(", c.").append(d.columnaPadre()).append(" AS idPadre")
               .append(", p.").append(p.columnaNombre()).append(" AS nombrePadre")
               .append(" FROM PROYECTO_UBA.").append(d.tabla()).append(" c")
               .append(" LEFT JOIN PROYECTO_UBA.").append(p.tabla()).append(" p")
               .append(" ON p.").append(p.columnaId()).append(" = c.").append(d.columnaPadre());
        } else {
            sql.append(" FROM PROYECTO_UBA.").append(d.tabla()).append(" c");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (d.tienePadre() && idPadre != null) {
            sql.append(" WHERE c.").append(d.columnaPadre()).append(" = :padre");
            params.addValue("padre", idPadre);
        }

        sql.append(" ORDER BY c.").append(d.columnaNombre());

        return jdbc.query(sql.toString(), params, (rs, i) -> {

            ItemCatalogo item = new ItemCatalogo();
            item.setId(rs.getLong("id"));
            item.setNombre(rs.getString("nombre"));

            if (d.tieneDescripcion()) {
                item.setDescripcion(rs.getString("descripcion"));
            }

            if (d.tienePadre()) {
                long padre = rs.getLong("idPadre");
                item.setIdPadre(rs.wasNull() ? null : padre);
                item.setNombrePadre(rs.getString("nombrePadre"));
            }

            return item;
        });
    }

    // ----------------------------------------------------------------- crear

    @Transactional
    public ItemCatalogo crear(String clave, ItemCatalogo datos) {

        DefinicionCatalogo d = registro.buscar(clave);
        validar(d, datos);

        List<String> columnas = new ArrayList<>(List.of(d.columnaNombre()));
        List<String> valores  = new ArrayList<>(List.of(":nombre"));

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nombre", datos.getNombre().trim());

        if (d.tienePadre()) {
            columnas.add(d.columnaPadre());
            valores.add(":padre");
            params.addValue("padre", datos.getIdPadre());
        }

        if (d.tieneDescripcion()) {
            columnas.add(d.columnaDescripcion());
            valores.add(":descripcion");
            params.addValue("descripcion",
                    datos.getDescripcion() == null ? "" : datos.getDescripcion().trim());
        }

        String sql = "INSERT INTO PROYECTO_UBA." + d.tabla()
                + " (" + String.join(", ", columnas) + ")"
                + " OUTPUT INSERTED." + d.columnaId()
                + " VALUES (" + String.join(", ", valores) + ")";

        try {
            Long id = jdbc.queryForObject(sql, params, Long.class);
            datos.setId(id);
            return datos;

        } catch (DataIntegrityViolationException e) {
            throw new ReglaNegocioException(
                    "Ya existe un registro con ese nombre en " + d.etiqueta().toLowerCase());
        }
    }

    // -------------------------------------------------------------- actualizar

    @Transactional
    public ItemCatalogo actualizar(String clave, Long id, ItemCatalogo datos) {

        DefinicionCatalogo d = registro.buscar(clave);
        validar(d, datos);

        List<String> asignaciones = new ArrayList<>(List.of(d.columnaNombre() + " = :nombre"));

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nombre", datos.getNombre().trim())
                .addValue("id", id);

        if (d.tienePadre()) {
            asignaciones.add(d.columnaPadre() + " = :padre");
            params.addValue("padre", datos.getIdPadre());
        }

        if (d.tieneDescripcion()) {
            asignaciones.add(d.columnaDescripcion() + " = :descripcion");
            params.addValue("descripcion",
                    datos.getDescripcion() == null ? "" : datos.getDescripcion().trim());
        }

        String sql = "UPDATE PROYECTO_UBA." + d.tabla()
                + " SET " + String.join(", ", asignaciones)
                + " WHERE " + d.columnaId() + " = :id";

        if (jdbc.update(sql, params) == 0) {
            throw new RecursoNoEncontradoException(
                    "No existe el registro " + id + " en " + d.etiqueta().toLowerCase());
        }

        datos.setId(id);
        return datos;
    }

    // ---------------------------------------------------------------- borrar

    @Transactional
    public void borrar(String clave, Long id) {

        DefinicionCatalogo d = registro.buscar(clave);

        // El uso se comprueba ANTES de borrar, y no se confia en que la base
        // rechace la operacion: casi todas las claves foraneas del esquema son
        // ON DELETE CASCADE, asi que borrar una especie se llevaba por delante
        // sus niveles, sus evidencias y su vinculo con los tramites, en
        // silencio y devolviendo 200.
        Map<String, Long> usos = contarUsos(d, id);

        if (!usos.isEmpty()) {

            String detalle = usos.entrySet().stream()
                    .map(e -> e.getValue() + " en " + legible(e.getKey()))
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

            throw new ReglaNegocioException(
                    "No se puede eliminar: está en uso (" + detalle + "). "
                    + "Cambia esos registros primero, o corrige el nombre en vez de borrarlo.");
        }

        String sql = "DELETE FROM PROYECTO_UBA." + d.tabla()
                + " WHERE " + d.columnaId() + " = :id";

        try {
            if (jdbc.update(sql, new MapSqlParameterSource("id", id)) == 0) {
                throw new RecursoNoEncontradoException(
                        "No existe el registro " + id + " en " + d.etiqueta().toLowerCase());
            }

        } catch (DataIntegrityViolationException e) {
            throw new ReglaNegocioException(
                    "No se puede eliminar: hay registros que lo están usando.");
        }
    }

    /**
     * Cuenta quien apunta a esta fila, tabla por tabla.
     *
     * Las tablas que referencian se descubren en sys.foreign_keys y no en una
     * lista fija: asi sigue siendo correcto si el esquema cambia.
     */
    private Map<String, Long> contarUsos(DefinicionCatalogo d, Long id) {

        List<Map<String, Object>> referencias = jdbc.queryForList("""
                SELECT OBJECT_NAME(f.parent_object_id) AS tabla,
                       c.name AS columna
                FROM sys.foreign_keys f
                JOIN sys.foreign_key_columns fc
                  ON fc.constraint_object_id = f.object_id
                JOIN sys.columns c
                  ON c.object_id = fc.parent_object_id
                 AND c.column_id = fc.parent_column_id
                WHERE OBJECT_NAME(f.referenced_object_id) = :tabla
                """, new MapSqlParameterSource("tabla", d.tabla()));

        Map<String, Long> usos = new java.util.LinkedHashMap<>();

        for (Map<String, Object> ref : referencias) {

            String tabla = (String) ref.get("tabla");
            String columna = (String) ref.get("columna");

            // Los nombres vienen del catalogo del sistema, no del usuario.
            Long cuantos = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM PROYECTO_UBA." + tabla
                    + " WHERE " + columna + " = :id",
                    new MapSqlParameterSource("id", id), Long.class);

            if (cuantos != null && cuantos > 0) {
                usos.merge(tabla, cuantos, Long::sum);
            }
        }

        return usos;
    }

    /** "UBA_TRAMITE_ESPECIE" -> "tramite especie", para el mensaje de error. */
    private static String legible(String tabla) {
        return tabla.replaceFirst("^UBA_", "").replace('_', ' ').toLowerCase();
    }

    // ------------------------------------------------------------- utilidades

    private void validar(DefinicionCatalogo d, ItemCatalogo datos) {

        if (datos == null || datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new ReglaNegocioException("El nombre es obligatorio");
        }

        if (datos.getNombre().trim().length() > 150) {
            throw new ReglaNegocioException("El nombre no puede superar los 150 caracteres");
        }

        if (d.tienePadre() && datos.getIdPadre() == null) {
            DefinicionCatalogo p = registro.buscar(d.clavePadre());
            throw new ReglaNegocioException(
                    "Debes elegir " + p.etiqueta().toLowerCase());
        }
    }

    /**
     * Devuelve el identificador de una fila a partir de un valor que puede ser
     * ya el identificador o el nombre. Las pantallas antiguas mandan el nombre.
     */
    public Long resolverId(String clave, String valor) {

        if (valor == null || valor.isBlank()) return null;

        try {
            return Long.valueOf(valor);
        } catch (NumberFormatException ignorado) {
            // No es un numero: se busca por nombre.
        }

        DefinicionCatalogo d = registro.buscar(clave);

        List<Long> encontrados = jdbc.queryForList(
                "SELECT " + d.columnaId() + " FROM PROYECTO_UBA." + d.tabla()
                + " WHERE " + d.columnaNombre() + " = :nombre",
                new MapSqlParameterSource("nombre", valor), Long.class);

        return encontrados.isEmpty() ? null : encontrados.get(0);
    }

    public List<Map<String, Object>> definiciones() {

        List<Map<String, Object>> salida = new ArrayList<>();

        for (DefinicionCatalogo d : registro.todos()) {

            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("clave", d.clave());
            m.put("etiqueta", d.etiqueta());
            m.put("clavePadre", d.clavePadre());
            m.put("tieneDescripcion", d.tieneDescripcion());

            if (d.clavePadre() != null) {
                m.put("etiquetaPadre", registro.buscar(d.clavePadre()).etiqueta());
            }

            salida.add(m);
        }

        return salida;
    }
}
