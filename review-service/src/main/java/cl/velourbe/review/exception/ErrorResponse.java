package cl.velourbe.review.exception;

import java.time.LocalDateTime;

/**
 * Uniform error payload returned by the review service.
 */
public record ErrorResponse(int status, String message, LocalDateTime timestamp) {}
