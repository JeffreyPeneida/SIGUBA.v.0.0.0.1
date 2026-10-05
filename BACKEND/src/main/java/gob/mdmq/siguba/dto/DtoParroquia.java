package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import gob.mdmq.siguba.Entidades.AdminZonal;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoParroquia implements Comparable<DtoParroquia> {

    private Long idParroquia;
    private String nombre;
    private AdminZonal adminZonal;

    public DtoParroquia() {
    }

    public DtoParroquia(Long idParroquia,
                        String nombre,
                        AdminZonal adminZonal) {

        this.idParroquia = idParroquia;
        this.nombre = nombre;
        this.adminZonal = adminZonal;
    }

    public Long getIdParroquia() {
        return idParroquia;
    }

    public void setIdParroquia(Long idParroquia) {
        this.idParroquia = idParroquia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public AdminZonal getAdminZonal() {
        return adminZonal;
    }

    public void setAdminZonal(AdminZonal adminZonal) {
        this.adminZonal = adminZonal;
    }

    @Override
    public int compareTo(DtoParroquia o) {

        if (o == null) {
            return 1;
        }

        if (this.idParroquia == null && o.idParroquia == null) {
            return 0;
        }

        if (this.idParroquia == null) {
            return -1;
        }

        if (o.idParroquia == null) {
            return 1;
        }

        return this.idParroquia.compareTo(o.idParroquia);
    }
}