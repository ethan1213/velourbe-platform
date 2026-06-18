package cl.velourbe.rental.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad del scooter-rental-service.
 * Define las reglas de acceso por endpoint y registra el filtro JWT
 * que valida el token en cada request entrante.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Define la cadena de filtros de seguridad HTTP:
     * - Sin CSRF (API stateless)
     * - Sin sesiones (JWT)
     * - Swagger accesible sin autenticación
     * - /api/scooters/** y /api/rentals/long restringidos a ADMIN
     * - /api/rentals/** requiere autenticación (cualquier rol)
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
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**",
                                 "/swagger-ui.html", "/actuator/**").permitAll()
                // Patinetas disponibles: cualquier usuario autenticado puede verlas para arrendar
                .requestMatchers("/api/scooters/available").authenticated()
                // Resto de gestión de patinetas: solo ADMIN
                .requestMatchers("/api/scooters/**").hasRole("ADMIN")
                .requestMatchers("/api/rentals/long").hasRole("ADMIN")
                .requestMatchers("/api/rentals/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
