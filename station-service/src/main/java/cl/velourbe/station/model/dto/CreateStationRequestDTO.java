package cl.velourbe.station.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating a new station.
 */
public record CreateStationRequestDTO(
        @NotBlank(message = "name es requerido") String name,
        @NotBlank(message = "address es requerido") String address,
        @NotNull(message = "latitude es requerida")
        @Min(value = -90, message = "latitude debe ser >= -90")
        @Max(value = 90, message = "latitude debe ser <= 90") Double latitude,
        @NotNull(message = "longitude es requerida")
        @Min(value = -180, message = "longitude debe ser >= -180")
        @Max(value = 180, message = "longitude debe ser <= 180") Double longitude,
        @NotNull(message = "capacity es requerida")
        @Min(value = 1, message = "capacity debe ser >= 1") Integer capacity
) {}
