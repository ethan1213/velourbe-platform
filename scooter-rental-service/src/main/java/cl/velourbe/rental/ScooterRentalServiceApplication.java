package cl.velourbe.rental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio de arriendos de VeloUrbe.
 * Corre en el puerto 8082 y gestiona el inventario de patinetas
 * y el ciclo de vida completo de los arriendos.
 */
@SpringBootApplication
public class ScooterRentalServiceApplication {

    /**
     * Inicia el contexto de Spring Boot.
     *
     * @param args argumentos de línea de comandos (no requeridos)
     */
    public static void main(String[] args) {
        SpringApplication.run(ScooterRentalServiceApplication.class, args);
    }
}
