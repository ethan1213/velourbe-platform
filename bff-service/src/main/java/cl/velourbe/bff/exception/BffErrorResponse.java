package cl.velourbe.bff.exception;

import java.time.LocalDateTime;

/**
 * Uniform error response shape used by the centralized BFF error handler.
 */
public record BffErrorResponse(
        int status,
        String code,
        String message,
        LocalDateTime timestamp
) {}
