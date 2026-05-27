package cl.velourbe.userauth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad del user-auth-service.
 * Define las reglas de acceso por endpoint, el modo stateless (sin sesiones HTTP)
 * y registra el filtro JWT que valida el token en cada request entrante.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Define la cadena de filtros de seguridad HTTP:
     * - Sin CSRF (API stateless)
     * - Sin sesiones HTTP (JWT)
     * - /api/auth/** y Swagger accesibles sin autenticación
     * - /api/users/** restringido a rol ADMIN
     *
     * @param http objeto de configuración de Spring Security
     * @return cadena de filtros configurada
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/swagger-ui/**",
                                 "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Define el encoder de contraseñas con BCrypt (factor de costo 10).
     * Se usa para hashear contraseñas al registrar y para verificarlas al hacer login.
     *
     * @return instancia de BCryptPasswordEncoder con factor 10
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
