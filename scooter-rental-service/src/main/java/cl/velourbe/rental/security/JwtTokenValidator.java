package cl.velourbe.rental.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Componente responsable de validar los tokens JWT emitidos por el user-auth-service.
 * Utiliza el mismo secret configurado en ambos servicios para verificar la firma HS256.
 * No genera tokens: solo los lee y verifica.
 */
@Component
public class JwtTokenValidator {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Construye la clave HMAC a partir del secret compartido con user-auth-service.
     * Ambos servicios deben usar exactamente el mismo secret para que los tokens sean válidos.
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Valida la firma del token y retorna el payload si es correcto.
     *
     * @param token token JWT compacto recibido en el header Authorization
     * @return claims del payload (email, role, userId)
     * @throws JwtException si la firma es inválida o el token está expirado
     */
    public Claims validate(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Verifica si un token es válido sin lanzar excepción.
     *
     * @param token token JWT a verificar
     * @return {@code true} si es válido y no expirado, {@code false} en caso contrario
     */
    public boolean isValid(String token) {
        try {
            validate(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
