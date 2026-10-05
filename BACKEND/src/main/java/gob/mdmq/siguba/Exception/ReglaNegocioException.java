package gob.mdmq.siguba.Exception;

/** La peticion es valida pero incumple una regla de negocio. HTTP 400. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
