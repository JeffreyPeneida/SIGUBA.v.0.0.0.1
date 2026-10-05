package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Credenciales enviadas en el cuerpo de POST /api/login. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DtoLogin {

    private String usuario;
    private String password;

    public DtoLogin() {
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
