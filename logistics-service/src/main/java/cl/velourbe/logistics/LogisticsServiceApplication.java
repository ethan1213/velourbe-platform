package cl.velourbe.logistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Logistics Service Application
 * Main entry point for the Spring Boot logistics microservice.
 */
@SpringBootApplication
public class LogisticsServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(LogisticsServiceApplication.class, args);
    }
}
