package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoPredio implements Comparable<DtoPredio> {

    private Long idPredio;
    private String nombre;

    public DtoPredio() {
    }

    public DtoPredio(Long idPredio, String nombre) {
        this.idPredio = idPredio;
        this.nombre = nombre;
    }

    public Long getIdPredio() {
        return idPredio;
    }

    public void setIdPredio(Long idPredio) {
        this.idPredio = idPredio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public int compareTo(DtoPredio o) {

        if (o == null) {
            return 1;
        }

        if (this.idPredio == null && o.idPredio == null) {
            return 0;
        }

        if (this.idPredio == null) {
            return -1;
        }

        if (o.idPredio == null) {
            return 1;
        }

        return this.idPredio.compareTo(o.idPredio);
    }
}