package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Respuesta de POST /api/login: el token y los datos del usuario. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DtoSesion {

    private String token;
    private long expiraEnMs;
    private DtoUsuario usuario;

    public DtoSesion() {
    }

    public DtoSesion(String token, long expiraEnMs, DtoUsuario usuario) {
        this.token = token;
        this.expiraEnMs = expiraEnMs;
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiraEnMs() {
        return expiraEnMs;
    }

    public void setExpiraEnMs(long expiraEnMs) {
        this.expiraEnMs = expiraEnMs;
    }

    public DtoUsuario getUsuario() {
        return usuario;
    }

    public void setUsuario(DtoUsuario usuario) {
        this.usuario = usuario;
    }
}
