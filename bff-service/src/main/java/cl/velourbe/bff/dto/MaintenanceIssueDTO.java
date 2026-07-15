package cl.velourbe.bff.dto;

import java.time.LocalDateTime;

/**
 * Maintenance issue DTO consumed from maintenance-service.
 */
public record MaintenanceIssueDTO(
        Long id,
        Long scooterId,
        String issueType,
        String description,
        String status,
        String resolutionNotes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime resolvedAt
) {}
