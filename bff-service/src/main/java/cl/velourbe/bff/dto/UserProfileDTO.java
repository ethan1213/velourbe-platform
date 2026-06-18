package cl.velourbe.bff.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserProfileDTO {
    private Long id;
    private String email;
    private String fullName;
    private String role;
    private LocalDateTime createdAt;
    private Boolean active;
}
