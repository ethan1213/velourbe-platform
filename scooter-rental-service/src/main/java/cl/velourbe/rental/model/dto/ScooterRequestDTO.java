package cl.velourbe.rental.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ScooterRequestDTO {
    @NotBlank
    private String serialCode;
    @NotBlank
    private String model;
    @NotNull @Min(0) @Max(100)
    private Integer battery;
    @NotBlank
    private String location;
}
