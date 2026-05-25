package cl.velourbe.rental.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScooterResponseDTO {
    private Long id;
    private String serialCode;
    private String model;
    private Integer battery;
    private String location;
    private String status;
    private LocalDateTime createdAt;
}
