package cl.velourbe.station.model.dto;

import java.time.LocalDateTime;

/**
 * DTO for station responses.
 */
public record StationResponseDTO(
        Long id,
        String name,
        String address,
        Double latitude,
        Double longitude,
        Integer capacity,
        Integer occupied,
        Integer availableSlots,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
