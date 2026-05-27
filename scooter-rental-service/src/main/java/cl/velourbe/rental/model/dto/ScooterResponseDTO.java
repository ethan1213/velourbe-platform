package cl.velourbe.rental.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO de salida que representa los datos de una patineta.
 * Usado como respuesta en todos los endpoints de {@code /api/scooters}.
 */
@Data
public class ScooterResponseDTO {

    /** Identificador único de la patineta. */
    private Long id;

    /** Código de serie físico único. */
    private String serialCode;

    /** Modelo comercial de la patineta. */
    private String model;

    /** Porcentaje de batería actual (0–100). */
    private Integer battery;

    /** Ubicación física actual. */
    private String location;

    /** Estado como texto: "AVAILABLE", "IN_USE" o "MAINTENANCE". */
    private String status;

    /** Fecha y hora de registro en el sistema. */
    private LocalDateTime createdAt;
}
