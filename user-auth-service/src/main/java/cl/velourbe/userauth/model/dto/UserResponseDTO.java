package cl.velourbe.userauth.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO de salida que representa los datos públicos de un usuario.
 * No expone el hash de la contraseña. Usado por {@code GET /api/users}.
 */
@Data
public class UserResponseDTO {

    /** Identificador único del usuario. */
    private Long id;

    /** Dirección de correo electrónico. */
    private String email;

    /** Nombre completo del usuario. */
    private String fullName;

    /** Rol de autorización: "CLIENT" o "ADMIN". */
    private String role;

    /** Fecha y hora de creación del registro. */
    private LocalDateTime createdAt;

    /** Indica si el usuario está activo en el sistema. */
    private Boolean active;
}
