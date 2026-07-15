package cl.velourbe.support.model.dto;

import cl.velourbe.support.model.enums.TicketCategory;
import cl.velourbe.support.model.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a support ticket.
 */
public record CreateTicketRequestDTO(
        Long rentalId,
        @NotBlank(message = "subject es requerido")
        @Size(max = 200, message = "subject no puede superar 200 caracteres")
        String subject,
        @NotBlank(message = "description es requerido")
        String description,
        TicketPriority priority,
        TicketCategory category
) {}
