package cl.velourbe.logistics.model.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for scooter location information.
 * Used for API request/response payloads.
 */
public record LocationDTO(
    Long id,
    Long scooterId,
    Double latitude,
    Double longitude,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
