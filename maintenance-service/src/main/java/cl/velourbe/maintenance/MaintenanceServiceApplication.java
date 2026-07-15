package cl.velourbe.maintenance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Maintenance Service Application
 * Main entry point for the Spring Boot maintenance microservice.
 */
@SpringBootApplication
public class MaintenanceServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MaintenanceServiceApplication.class, args);
    }
}
