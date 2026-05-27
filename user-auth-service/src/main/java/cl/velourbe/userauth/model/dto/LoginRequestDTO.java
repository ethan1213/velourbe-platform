package cl.velourbe.userauth.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO de entrada para el endpoint {@code POST /api/auth/login}.
 * Contiene las credenciales de acceso del usuario.
 */
@Data
public class LoginRequestDTO {

    /** Dirección de correo electrónico del usuario. Debe ser un email válido. */
    @NotBlank @Email
    private String email;

    /** Contraseña en texto plano. Se verifica contra el hash BCrypt almacenado. */
    @NotBlank
    private String password;
}
