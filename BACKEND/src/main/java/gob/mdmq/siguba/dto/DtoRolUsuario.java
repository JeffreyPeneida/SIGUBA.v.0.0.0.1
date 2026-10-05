package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoRolUsuario implements Comparable<DtoRolUsuario> {

    private String rol;

    public DtoRolUsuario() {
    }

    public DtoRolUsuario(String rol) {
        this.rol = rol;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    @Override
    public int compareTo(DtoRolUsuario o) {

        if (o == null) {
            return 1;
        }

        if (this.rol == null && o.rol == null) {
            return 0;
        }

        if (this.rol == null) {
            return -1;
        }

        if (o.rol == null) {
            return 1;
        }

        return this.rol.compareTo(o.rol);
    }
}