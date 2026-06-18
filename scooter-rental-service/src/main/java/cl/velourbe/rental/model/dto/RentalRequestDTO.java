package cl.velourbe.rental.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO de entrada para iniciar un arriendo mediante {@code POST /api/rentals/start}.
 * El usuario que arrenda se obtiene del token JWT, no del body.
 */
@Data
public class RentalRequestDTO {

    /** ID de la patineta que se desea arrendar. Debe existir y estar disponible. */
    @NotNull
    private Long scooterId;
}
