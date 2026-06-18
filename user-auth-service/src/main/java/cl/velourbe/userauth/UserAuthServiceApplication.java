package cl.velourbe.userauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio de autenticación de VeloUrbe.
 * Corre en el puerto 8081 y gestiona registro, login y administración de usuarios.
 */
@SpringBootApplication
public class UserAuthServiceApplication {

    /**
     * Inicia el contexto de Spring Boot.
     *
     * @param args argumentos de línea de comandos (no requeridos)
     */
    public static void main(String[] args) {
        SpringApplication.run(UserAuthServiceApplication.class, args);
    }
}
