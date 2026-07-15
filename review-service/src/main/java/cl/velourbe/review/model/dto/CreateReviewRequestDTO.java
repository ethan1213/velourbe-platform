package cl.velourbe.review.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new review.
 */
public record CreateReviewRequestDTO(
        @NotNull(message = "rentalId es requerido") Long rentalId,
        @NotNull(message = "scooterId es requerido") Long scooterId,
        @NotNull(message = "rating es requerido")
        @Min(value = 1, message = "rating debe ser >= 1")
        @Max(value = 5, message = "rating debe ser <= 5") Integer rating,
        @Size(max = 1000, message = "comment no puede superar 1000 caracteres") String comment
) {}
