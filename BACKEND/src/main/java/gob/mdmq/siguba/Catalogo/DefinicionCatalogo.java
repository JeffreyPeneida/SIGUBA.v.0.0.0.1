package gob.mdmq.siguba.Catalogo;

/**
 * Descripcion de un catalogo administrable.
 *
 * Los 13 catalogos comparten forma: identificador, nombre y a veces un padre o
 * una descripcion. En vez de 13 controladores casi identicos, se describen aqui
 * y un unico CRUD generico los atiende.
 *
 * Los nombres de tabla y columna SOLO salen de este registro, nunca de la
 * peticion: es lo que hace seguro construir el SQL por concatenacion.
 *
 * @param clave        identificador en la URL, en minusculas
 * @param etiqueta     nombre para mostrar
 * @param tabla        tabla en el esquema PROYECTO_UBA
 * @param columnaId    clave primaria
 * @param columnaNombre columna con el texto visible
 * @param columnaPadre  clave foranea al catalogo padre, o null
 * @param clavePadre    clave del catalogo padre, o null
 * @param columnaDescripcion columna de texto largo opcional, o null
 */
public record DefinicionCatalogo(
        String clave,
        String etiqueta,
        String tabla,
        String columnaId,
        String columnaNombre,
        String columnaPadre,
        String clavePadre,
        String columnaDescripcion) {

    public boolean tienePadre() {
        return columnaPadre != null;
    }

    public boolean tieneDescripcion() {
        return columnaDescripcion != null;
    }
}
