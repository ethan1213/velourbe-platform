package cl.velourbe.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class DashboardResponseDTO {
    private UserProfileDTO profile;
    private List<RentalDTO> activeRentals;
    private List<RentalDTO> recentRentals;
}
