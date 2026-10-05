package gob.mdmq.siguba.dto;

public class DtoResponse {

    private int codigo;
    private String mensaje;
    private Object detalle;

    public DtoResponse() {
    }

    public DtoResponse(int codigo, String mensaje) {
        this.codigo = codigo;
        this.mensaje = mensaje;
    }

    public DtoResponse(
            int codigo,
            String mensaje,
            Object detalle) {

        this.codigo = codigo;
        this.mensaje = mensaje;
        this.detalle = detalle;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Object getDetalle() {
        return detalle;
    }

    public void setDetalle(Object detalle) {
        this.detalle = detalle;
    }
}