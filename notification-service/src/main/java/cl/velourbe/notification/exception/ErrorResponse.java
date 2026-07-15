package cl.velourbe.notification.exception;

import java.time.LocalDateTime;
import lombok.*;

/**
 * Standard error response structure
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int code;
    private String message;
    private LocalDateTime timestamp;
}
