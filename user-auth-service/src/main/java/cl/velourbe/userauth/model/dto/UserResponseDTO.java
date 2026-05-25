package cl.velourbe.userauth.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponseDTO {
    private Long id;
    private String email;
    private String fullName;
    private String role;
    private LocalDateTime createdAt;
    private Boolean active;
}
