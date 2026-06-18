package cl.velourbe.rental.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO de entrada para crear una nueva patineta mediante {@code POST /api/scooters}.
 * Todos los campos son obligatorios. El estado inicial siempre es AVAILABLE.
 */
@Data
public class ScooterRequestDTO {

    /** Código de serie único de la patineta (ej. "SC-001"). */
    @NotBlank
    private String serialCode;

    /** Modelo comercial (ej. "Xiaomi Pro 2"). */
    @NotBlank
    private String model;

    /** Nivel de batería actual en porcentaje. Debe estar entre 0 y 100. */
    @NotNull @Min(0) @Max(100)
    private Integer battery;

    /** Ubicación física actual de la patineta. */
    @NotBlank
    private String location;
}
