package cl.velourbe.userauth.config;

import cl.velourbe.userauth.model.entity.User;
import cl.velourbe.userauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Generador de usuarios de prueba con rol CLIENT.
 * <p>
 * Solo se activa con el perfil {@code dev}. Usa DataFaker para crear usuarios
 * realistas al arrancar, con contraseñas hasheadas mediante el mismo
 * {@link PasswordEncoder} (BCrypt) configurado en {@link SecurityConfig}.
 * <p>
 * No toca al usuario administrador sembrado por Flyway: solo se ejecuta cuando
 * la tabla contiene únicamente al admin (un registro o menos).
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 1) {
            log.info("DataLoader omitido: ya existen usuarios de prueba en la base de datos");
            return;
        }

        Faker faker = new Faker(new Locale("es", "CL"));

        for (int i = 0; i < 10; i++) {
            String fullName = faker.name().fullName();
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(buildEmail(fullName, i));
            user.setPasswordHash(passwordEncoder.encode("cliente123"));
            user.setRole("CLIENT");
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
        log.info("DataLoader: 10 usuarios CLIENT de prueba generados (password: cliente123)");
    }

    /** Construye un email único y limpio a partir del nombre generado. */
    private String buildEmail(String fullName, int index) {
        String slug = fullName.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z ]", "")
                .trim()
                .replaceAll("\\s+", ".");
        return slug + (index + 1) + "@velourbe.cl";
    }
}
