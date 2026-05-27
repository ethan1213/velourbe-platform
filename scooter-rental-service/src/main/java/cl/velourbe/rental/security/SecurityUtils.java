package cl.velourbe.rental.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utilidad estática para acceder a los datos del usuario autenticado
 * desde el {@link SecurityContextHolder}.
 * Los datos son inyectados por {@code JwtAuthFilter} al validar el token.
 */
public class SecurityUtils {

    /**
     * Retorna el email del usuario autenticado en el contexto de seguridad actual.
     *
     * @return email del usuario autenticado, o {@code null} si no hay sesión activa
     */
    public static String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? (String) auth.getPrincipal() : null;
    }

    /**
     * Retorna el ID numérico del usuario autenticado, almacenado en los detalles
     * del token por {@code JwtAuthFilter}.
     *
     * @return ID del usuario autenticado, o {@code null} si no hay sesión activa
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof Long) {
            return (Long) auth.getDetails();
        }
        return null;
    }
}
