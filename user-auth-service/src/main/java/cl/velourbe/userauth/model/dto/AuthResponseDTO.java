package cl.velourbe.userauth.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO de respuesta devuelto tras un registro o login exitoso.
 * Contiene el token JWT para autenticar futuras peticiones y el rol del usuario.
 */
@Data
@AllArgsConstructor
public class AuthResponseDTO {

    /** Token JWT firmado con HS256. Debe enviarse como {@code Authorization: Bearer <token>}. */
    private String token;

    /** Rol del usuario autenticado: "CLIENT" o "ADMIN". */
    private String role;
}
