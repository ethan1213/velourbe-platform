package cl.velourbe.station;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Station Service Application.
 * Microservice for managing charging/parking stations for scooters.
 */
@SpringBootApplication
public class StationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StationServiceApplication.class, args);
    }
}
