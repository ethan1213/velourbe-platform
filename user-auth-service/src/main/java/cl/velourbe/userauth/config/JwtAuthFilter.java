package cl.velourbe.userauth.config;

import cl.velourbe.userauth.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

/**
 * Filtro JWT que se ejecuta una vez por request HTTP.
 * Intercepta el header {@code Authorization: Bearer <token>}, valida el token
 * con {@link JwtUtil} y, si es válido, registra al usuario en el
 * {@link SecurityContextHolder} para que Spring Security pueda evaluar permisos.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /**
     * Lógica principal del filtro: extrae el token del header, lo valida y,
     * si es correcto, establece la autenticación en el contexto de seguridad.
     * Si no hay token o es inválido, la cadena de filtros continúa sin autenticación.
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
            if (jwtUtil.isValid(token)) {
                Claims claims = jwtUtil.parseToken(token);
                String role = claims.get("role", String.class);
                var auth = new UsernamePasswordAuthenticationToken(
                        claims.getSubject(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(req, res);
    }
}
