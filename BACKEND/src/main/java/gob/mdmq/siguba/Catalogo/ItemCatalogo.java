package gob.mdmq.siguba.Catalogo;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Una fila de cualquier catalogo. Los campos que no apliquen viajan como null. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemCatalogo {

    private Long id;
    private String nombre;
    private Long idPadre;
    private String nombrePadre;
    private String descripcion;

    /** Cuantos registros lo usan: si es > 0 no se puede borrar. */
    private Long enUso;

    public ItemCatalogo() {
    }

    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }

    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }

    public Long getIdPadre() { return idPadre; }
    public void setIdPadre(Long v) { this.idPadre = v; }

    public String getNombrePadre() { return nombrePadre; }
    public void setNombrePadre(String v) { this.nombrePadre = v; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { this.descripcion = v; }

    public Long getEnUso() { return enUso; }
    public void setEnUso(Long v) { this.enUso = v; }
}
