package cl.velourbe.maintenance.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO for maintenance issue requests and responses.
 */
public record MaintenanceIssueDTO(
    Long id,
    
    @NotNull(message = "scooterId es requerido")
    Long scooterId,
    
    @NotBlank(message = "issueType es requerido")
    String issueType,
    
    @NotBlank(message = "description es requerida")
    String description,
    
    String status,
    
    String resolutionNotes,
    
    LocalDateTime createdAt,
    
    LocalDateTime updatedAt,
    
    LocalDateTime resolvedAt
) {}
