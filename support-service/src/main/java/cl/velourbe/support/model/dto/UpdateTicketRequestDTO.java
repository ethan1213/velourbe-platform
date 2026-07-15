package cl.velourbe.support.model.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO for admin updates: assigning or resolving a ticket.
 */
public record UpdateTicketRequestDTO(
        String assignedTo,
        @Size(max = 4000, message = "resolutionNotes no puede superar 4000 caracteres")
        String resolutionNotes
) {}
