package cl.velourbe.payment.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT utility component for token validation.
 * Uses HS256 algorithm with shared secret from environment.
 * Parses and validates JWT tokens from API Gateway and BFF.
 */
@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration-ms:3600000}")
    private long expirationMs;
    
    /**
     * Constructs the HMAC key from configured secret.
     * Secret must be at least 32 characters (256 bits) for HS256.
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Parses and verifies the JWT token signature.
     * 
     * @param token JWT token to parse
     * @return claims from the token payload
     * @throws JwtException if token is invalid, expired, or signature incorrect
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(getKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
    
    /**
     * Validates if a token is valid (correct signature and not expired).
     * 
     * @param token JWT token to validate
     * @return true if token is valid, false otherwise
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
