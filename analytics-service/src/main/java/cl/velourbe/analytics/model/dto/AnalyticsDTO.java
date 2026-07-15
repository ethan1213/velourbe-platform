package cl.velourbe.analytics.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for analytics event responses.
 */
public record AnalyticsDTO(
    Long id,
    Long rentalId,
    Long userId,
    BigDecimal amount,
    String eventType,
    String description,
    LocalDateTime createdAt
) {}
