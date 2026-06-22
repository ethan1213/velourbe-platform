package cl.velourbe.rental.config;

import cl.velourbe.rental.model.entity.Rental;
import cl.velourbe.rental.model.entity.Scooter;
import cl.velourbe.rental.model.enums.RentalStatus;
import cl.velourbe.rental.model.enums.ScooterStatus;
import cl.velourbe.rental.repository.RentalRepository;
import cl.velourbe.rental.repository.ScooterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Generador de datos de prueba para el inventario de patinetas y arriendos.
 * <p>
 * Solo se activa con el perfil {@code dev} (no afecta producción ni Docker, que usan
 * el perfil por defecto). Usa la biblioteca DataFaker para poblar la base de datos
 * con información realista al arrancar la aplicación, facilitando pruebas manuales.
 * <p>
 * Se ejecuta únicamente si las tablas están vacías, para no duplicar registros en
 * reinicios sucesivos ni chocar con las migraciones de Flyway.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final ScooterRepository scooterRepository;
    private final RentalRepository rentalRepository;

    @Override
    public void run(String... args) {
        if (scooterRepository.count() > 0) {
            log.info("DataLoader omitido: ya existen patinetas en la base de datos");
            return;
        }

        Faker faker = new Faker(new Locale("es", "CL"));
        Random random = new Random();

        // Generar 15 patinetas con datos falsos
        List<Scooter> scooters = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Scooter scooter = new Scooter();
            scooter.setSerialCode(String.format("SC-%04d", i + 1));
            scooter.setModel(faker.options().option(
                    "Xiaomi Pro 2", "Xiaomi Pro 4", "Segway Ninebot E2",
                    "Segway Max G30", "Ninebot F40", "Okai ES400"));
            scooter.setBattery(faker.number().numberBetween(10, 100));
            scooter.setLocation(faker.address().streetName() + ", " + faker.address().city());
            scooter.setStatus(ScooterStatus.AVAILABLE);
            scooter.setCreatedAt(LocalDateTime.now());
            scooters.add(scooterRepository.save(scooter));
        }
        log.info("DataLoader: {} patinetas de prueba generadas", scooters.size());

        // Generar 10 arriendos de muestra (algunos completados, algunos activos)
        for (int i = 0; i < 10; i++) {
            Scooter scooter = scooters.get(random.nextInt(scooters.size()));
            Rental rental = new Rental();
            // userId simulado: en producción proviene del token JWT
            rental.setUserId((long) faker.number().numberBetween(1, 11));
            rental.setScooter(scooter);
            LocalDateTime startedAt = LocalDateTime.now().minusHours(faker.number().numberBetween(1, 48));
            rental.setStartedAt(startedAt);

            boolean completed = random.nextBoolean();
            if (completed) {
                int minutes = faker.number().numberBetween(5, 120);
                rental.setEndedAt(startedAt.plusMinutes(minutes));
                rental.setStatus(RentalStatus.COMPLETED);
                rental.setTotalMinutes(minutes);
            } else {
                rental.setStatus(RentalStatus.ACTIVE);
            }
            rentalRepository.save(rental);
        }
        log.info("DataLoader: 10 arriendos de prueba generados");
    }
}
