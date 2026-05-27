package cl.velourbe.rental.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Estructura estándar de respuesta de error devuelta por el {@link GlobalExceptionHandler}.
 * Todos los errores de la API siguen este formato JSON.
 */
@Data
@AllArgsConstructor
public class ErrorResponse {

    /** Código HTTP del error (ej. 404, 409, 500). */
    private int status;

    /** Mensaje descriptivo del error en inglés. */
    private String message;

    /** Marca de tiempo en que ocurrió el error. */
    private LocalDateTime timestamp;
}
