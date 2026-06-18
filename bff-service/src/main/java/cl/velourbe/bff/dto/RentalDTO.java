package cl.velourbe.bff.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RentalDTO {
    private Long id;
    private Long userId;
    private Long scooterId;
    private String scooterModel;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String status;
    private Integer totalMinutes;
}
