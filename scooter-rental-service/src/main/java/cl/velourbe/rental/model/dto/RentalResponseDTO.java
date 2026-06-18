package cl.velourbe.rental.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO de salida que representa el estado de un arriendo.
 * Usado como respuesta en todos los endpoints de {@code /api/rentals}.
 */
@Data
public class RentalResponseDTO {

    /** Identificador único del arriendo. */
    private Long id;

    /** ID del usuario que realizó el arriendo (del token JWT, no FK). */
    private Long userId;

    /** ID de la patineta arrendada. */
    private Long scooterId;

    /** Modelo de la patineta arrendada, incluido para contexto visual. */
    private String scooterModel;

    /** Momento en que comenzó el arriendo. */
    private LocalDateTime startedAt;

    /** Momento en que terminó el arriendo. Null si aún está activo. */
    private LocalDateTime endedAt;

    /** Estado del arriendo: "ACTIVE", "COMPLETED" o "CANCELLED". */
    private String status;

    /** Duración total calculada en minutos. Null si el arriendo aún está activo. */
    private Integer totalMinutes;
}
