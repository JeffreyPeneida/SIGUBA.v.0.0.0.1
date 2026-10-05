package gob.mdmq.siguba.Exception;

import gob.mdmq.siguba.dto.DtoResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Traduce las excepciones a codigos HTTP coherentes y deja el detalle tecnico en
 * el log en vez de en la respuesta.
 *
 * Antes cada controlador atrapaba Exception por su cuenta y devolvia mensajes
 * como "Transaction silently rolled back", que ocultaban la causa real, o un
 * 201 aunque la operacion hubiese fallado.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<DtoResponse> noEncontrado(RecursoNoEncontradoException e) {

        return responder(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<DtoResponse> reglaNegocio(ReglaNegocioException e) {

        return responder(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentNotValidException.class,
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class
    })
    public ResponseEntity<DtoResponse> peticionInvalida(Exception e) {

        log.warn("Peticion invalida: {}", e.getMessage());

        return responder(HttpStatus.BAD_REQUEST,
                "La peticion no tiene el formato esperado");
    }

    /**
     * Una URL que no corresponde a ningun controlador debe ser 404, no 500.
     * Sin esto el manejador generico de abajo la convertia en error interno.
     */
    @ExceptionHandler({
        NoResourceFoundException.class,
        NoHandlerFoundException.class
    })
    public ResponseEntity<DtoResponse> rutaNoEncontrada(Exception e) {

        return responder(HttpStatus.NOT_FOUND, "El recurso solicitado no existe");
    }

    /**
     * Las reglas por ruta de SecurityConfig ya devuelven 403 desde el filtro,
     * pero @PreAuthorize lanza dentro del metodo y caia en el manejador
     * generico: un permiso denegado se reportaba como error interno.
     */
    @ExceptionHandler({
        AuthorizationDeniedException.class,
        AccessDeniedException.class
    })
    public ResponseEntity<DtoResponse> sinPermiso(Exception e) {

        return responder(HttpStatus.FORBIDDEN, "No tienes permisos para hacer esto");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<DtoResponse> integridad(DataIntegrityViolationException e) {

        log.warn("Violacion de integridad", e);

        return responder(HttpStatus.CONFLICT,
                "La operacion viola una restriccion de la base de datos");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<DtoResponse> inesperada(Exception e) {

        // El detalle va al log; al cliente solo un mensaje generico.
        log.error("Error no controlado", e);

        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "Se produjo un error interno");
    }

    private ResponseEntity<DtoResponse> responder(HttpStatus estado, String mensaje) {

        return ResponseEntity
                .status(estado)
                .body(new DtoResponse(estado.value(), mensaje));
    }
}
