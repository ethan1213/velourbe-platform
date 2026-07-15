package cl.velourbe.support.model.dto;

import java.time.LocalDateTime;

/**
 * DTO for support ticket responses.
 */
public record SupportTicketResponseDTO(
        Long id,
        Long userId,
        Long rentalId,
        String subject,
        String description,
        String status,
        String priority,
        String category,
        String assignedTo,
        String resolutionNotes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
