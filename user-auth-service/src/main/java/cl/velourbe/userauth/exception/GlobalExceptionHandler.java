package cl.velourbe.userauth.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

/**
 * Manejador global de excepciones del user-auth-service.
 * Centraliza el manejo de errores y garantiza respuestas HTTP consistentes
 * con el formato {@link ErrorResponse} para todos los endpoints.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja intentos de registro con un email ya existente.
     *
     * @return 409 Conflict con el email duplicado en el mensaje
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailExists(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, ex.getMessage(), LocalDateTime.now()));
    }

    /**
     * Maneja credenciales inválidas durante el login.
     *
     * @return 401 Unauthorized
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCreds(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(401, ex.getMessage(), LocalDateTime.now()));
    }

    /**
     * Captura cualquier excepción no controlada y evita exponer detalles internos.
     *
     * @return 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Internal server error", LocalDateTime.now()));
    }
}
