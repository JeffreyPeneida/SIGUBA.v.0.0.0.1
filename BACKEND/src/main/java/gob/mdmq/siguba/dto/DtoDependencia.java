package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoDependencia implements Comparable<DtoDependencia> {

    private Long idDependencia;

    private String nombre;

    private String tipoDenunciante;

    public DtoDependencia() {
    }

    public DtoDependencia(
            Long idDependencia,
            String nombre,
            String tipoDenunciante) {

        this.idDependencia = idDependencia;
        this.nombre = nombre;
        this.tipoDenunciante = tipoDenunciante;
    }

    public Long getIdDependencia() {
        return idDependencia;
    }

    public void setIdDependencia(Long idDependencia) {
        this.idDependencia = idDependencia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoDenunciante() {
        return tipoDenunciante;
    }

    public void setTipoDenunciante(String tipoDenunciante) {
        this.tipoDenunciante = tipoDenunciante;
    }

    @Override
    public int compareTo(DtoDependencia o) {

        if (o == null) {
            return 1;
        }

        if (this.idDependencia == null && o.idDependencia == null) {
            return 0;
        }

        if (this.idDependencia == null) {
            return -1;
        }

        if (o.idDependencia == null) {
            return 1;
        }

        return this.idDependencia.compareTo(o.idDependencia);
    }

}