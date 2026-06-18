package cl.velourbe.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RentalSummaryDTO {
    private Long userId;
    private int totalRentals;
    private int completedRentals;
    private int activeRentals;
    private int totalMinutes;
}
