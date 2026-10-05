package gob.mdmq.siguba.Catalogo;

import gob.mdmq.siguba.Exception.RecursoNoEncontradoException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Los catalogos que se pueden administrar.
 *
 * Anadir uno nuevo es anadir una linea aqui: no hace falta tocar el controlador
 * ni el servicio. Lo que no este en esta lista no es accesible, asi que tambien
 * hace de lista blanca frente a nombres de tabla venidos de fuera.
 */
@Component
public class RegistroCatalogos {

    private final Map<String, DefinicionCatalogo> porClave = new LinkedHashMap<>();

    public RegistroCatalogos() {

        // --- simples: identificador y nombre ---------------------------------
        registrar("admin-zonal",      "Administraciones zonales", "UBA_ADMIN_ZONAL",       "ID_ADMIN_ZONAL",     null, null, null);
        registrar("especie",          "Especies (plagas)",        "UBA_ESPECIE",           "ID_ESPECIE",         null, null, null);
        registrar("motivo",           "Motivos de inspección",    "UBA_MOTIVO_INSPECCION", "ID_MOTIVO",          null, null, null);
        registrar("area",             "Áreas de inspección",      "UBA_AREA_INSPECCION",   "ID_AREA",            null, null, null);
        registrar("predio",           "Tipos de predio",          "UBA_PREDIO",            "ID_PREDIO",          null, null, null);
        registrar("tipo-denunciante", "Tipos de denunciante",     "UBA_TIPO_DENUNCIANTE",  "ID_TIPO",            null, null, null);
        registrar("tipo-parroquia",   "Tipos de parroquia",       "UBA_TIPO_PARROQUIA",    "ID_TIPO_PARROQUIA",  null, null, null);
        registrar("tipo-rodenticida", "Tipos de rodenticida",     "UBA_TIPO_RODENTICIDA",  "ID_TIPO",            null, null, null);

        // --- dependientes de otro catalogo -----------------------------------
        registrar("parroquia",   "Parroquias",          "UBA_PARROQUIA",      "ID_PARROQUIA",  "ID_ADMIN_ZONAL", "admin-zonal",      null);
        registrar("barrio",      "Barrios",             "UBA_BARRIO",         "ID_BARRIO",     "ID_PARROQUIA",   "parroquia",        null);
        registrar("dependencia", "Dependencias",        "UBA_DEPENDENCIA",    "ID_DEPENDENCIA","ID_TIPO",        "tipo-denunciante", null);
        registrar("evidencia",   "Tipos de evidencia",  "UBA_TIPO_EVIDENCIA", "ID_EVIDENCIA",  "ID_ESPECIE",     "especie",          null);

        // --- con descripcion --------------------------------------------------
        registrar("nivel-infestacion", "Niveles de infestación", "UBA_NIVEL_INFESTACION",
                  "ID_NIVEL", "ID_ESPECIE", "especie", "DESCRIPCION");
    }

    private void registrar(String clave, String etiqueta, String tabla, String columnaId,
                           String columnaPadre, String clavePadre, String columnaDescripcion) {

        // Todos los catalogos usan NOMBRE como texto visible.
        porClave.put(clave, new DefinicionCatalogo(
                clave, etiqueta, tabla, columnaId, "NOMBRE",
                columnaPadre, clavePadre, columnaDescripcion));
    }

    public List<DefinicionCatalogo> todos() {
        return List.copyOf(porClave.values());
    }

    public DefinicionCatalogo buscar(String clave) {

        DefinicionCatalogo d = porClave.get(clave);

        if (d == null) {
            throw new RecursoNoEncontradoException("No existe el catálogo " + clave);
        }

        return d;
    }
}
