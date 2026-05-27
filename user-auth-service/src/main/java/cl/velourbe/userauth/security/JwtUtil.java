package cl.velourbe.userauth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Componente responsable de generar, parsear y validar tokens JWT.
 * Utiliza el algoritmo HS256 (HMAC-SHA256) con el secret configurado en
 * {@code application.yml}. El mismo secret debe estar presente en el
 * scooter-rental-service para que los tokens sean interoperables.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms:3600000}")
    private long expirationMs;

    /**
     * Construye la clave HMAC a partir del secret configurado.
     * El secret debe tener al menos 32 caracteres (256 bits) para HS256.
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera un token JWT firmado con los datos del usuario.
     * El payload incluye: subject (email), claim "role" y claim "userId".
     *
     * @param email  dirección de correo del usuario (subject del token)
     * @param role   rol del usuario ("CLIENT" o "ADMIN")
     * @param userId identificador numérico del usuario
     * @return token JWT compacto listo para enviar en el header Authorization
     */
    public String generateToken(String email, String role, Long userId) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getKey())
                .compact();
    }

    /**
     * Parsea y verifica la firma del token, retornando el payload (claims).
     *
     * @param token token JWT compacto
     * @return claims del payload (subject, role, userId, iat, exp)
     * @throws JwtException si el token es inválido, expirado o tiene firma incorrecta
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Verifica si un token es válido (firma correcta y no expirado).
     *
     * @param token token JWT a verificar
     * @return {@code true} si el token es válido, {@code false} en caso contrario
     */
    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
