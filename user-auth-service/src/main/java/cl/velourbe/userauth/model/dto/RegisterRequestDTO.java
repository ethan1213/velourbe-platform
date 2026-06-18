package cl.velourbe.userauth.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO de entrada para el endpoint {@code POST /api/auth/register}.
 * Los usuarios registrados a través de este DTO reciben automáticamente el rol CLIENT.
 */
@Data
public class RegisterRequestDTO {

    /** Dirección de correo electrónico única que servirá como login. */
    @NotBlank @Email
    private String email;

    /** Contraseña elegida por el usuario. Se almacenará hasheada con BCrypt. */
    @NotBlank
    private String password;

    /** Nombre completo del usuario para identificación en el sistema. */
    @NotBlank
    private String fullName;
}
