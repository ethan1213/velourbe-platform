package cl.velourbe.rental.model.entity;

import cl.velourbe.rental.model.enums.ScooterStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scooters")
public class Scooter {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_code", nullable = false, unique = true, length = 50)
    private String serialCode;

    @Column(nullable = false, length = 80)
    private String model;

    @Column(nullable = false)
    private Integer battery;

    @Column(nullable = false, length = 100)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScooterStatus status = ScooterStatus.AVAILABLE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
