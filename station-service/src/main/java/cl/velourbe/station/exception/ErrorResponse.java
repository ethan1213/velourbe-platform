package cl.velourbe.station.exception;

import java.time.LocalDateTime;

/**
 * Uniform error payload returned by the station service.
 */
public record ErrorResponse(int status, String message, LocalDateTime timestamp) {}
