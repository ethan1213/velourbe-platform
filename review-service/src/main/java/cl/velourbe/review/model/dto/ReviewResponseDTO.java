package cl.velourbe.review.model.dto;

import java.time.LocalDateTime;

/**
 * DTO for review responses.
 */
public record ReviewResponseDTO(
        Long id,
        Long userId,
        Long rentalId,
        Long scooterId,
        Integer rating,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
