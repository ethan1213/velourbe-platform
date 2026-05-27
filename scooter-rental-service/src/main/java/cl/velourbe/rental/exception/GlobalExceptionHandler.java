package cl.velourbe.rental.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

/**
 * Manejador global de excepciones del scooter-rental-service.
 * Centraliza el manejo de errores y garantiza respuestas HTTP consistentes
 * con el formato {@link ErrorResponse} para todos los endpoints.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja búsquedas de patinetas que no existen en la base de datos.
     *
     * @return 404 Not Found
     */
    @ExceptionHandler(ScooterNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ScooterNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, ex.getMessage(), LocalDateTime.now()));
    }

    /**
     * Maneja intentos de arrendar una patineta que no está disponible.
     *
     * @return 409 Conflict
     */
    @ExceptionHandler(ScooterNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleNotAvailable(ScooterNotAvailableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, ex.getMessage(), LocalDateTime.now()));
    }

    /**
     * Maneja búsquedas de arriendos que no existen en la base de datos.
     *
     * @return 404 Not Found
     */
    @ExceptionHandler(RentalNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRentalNotFound(RentalNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, ex.getMessage(), LocalDateTime.now()));
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
