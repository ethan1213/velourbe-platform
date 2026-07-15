package cl.velourbe.bff.dto;

import java.time.LocalDateTime;

/**
 * DTO mirroring the SupportTicketResponseDTO of the support-service.
 * Used by the BFF to expose aggregated support data to the frontend.
 */
public record SupportTicketDTO(
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
