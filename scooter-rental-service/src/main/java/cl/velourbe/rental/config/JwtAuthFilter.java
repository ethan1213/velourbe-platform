package cl.velourbe.rental.config;

import cl.velourbe.rental.security.JwtTokenValidator;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

/**
 * Filtro JWT que se ejecuta una vez por request HTTP en el scooter-rental-service.
 * Valida el token emitido por user-auth-service con {@link JwtTokenValidator}
 * y registra el usuario (email, rol e ID) en el {@link SecurityContextHolder}.
 * Permite que {@code SecurityUtils} recupere el ID del usuario sin consultar otra base de datos.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenValidator jwtTokenValidator;

    /**
     * Lógica principal del filtro: extrae el token del header Authorization,
     * lo valida y establece la autenticación en el contexto de seguridad.
     * El {@code userId} extraído del token se almacena en los "details" de la autenticación
     * para que {@code SecurityUtils.getCurrentUserId()} pueda recuperarlo.
     *
     * @param req   request HTTP entrante
     * @param res   response HTTP saliente
     * @param chain cadena de filtros de Spring Security
     */
    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtTokenValidator.isValid(token)) {
                Claims claims = jwtTokenValidator.validate(token);
                String role = claims.get("role", String.class);
                Long userId = claims.get("userId", Long.class);
                var auth = new UsernamePasswordAuthenticationToken(
                        claims.getSubject(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                auth.setDetails(userId);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(req, res);
    }
}
