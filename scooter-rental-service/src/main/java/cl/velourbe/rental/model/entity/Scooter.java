package cl.velourbe.rental.model.entity;

import cl.velourbe.rental.model.enums.ScooterStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una patineta eléctrica del inventario.
 * Se mapea a la tabla {@code scooters} de la base de datos {@code db_scooter_rentals}.
 */
@Data
@Entity
@Table(name = "scooters")
public class Scooter {

    /** Identificador único autoincremental. */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código de serie físico grabado en la patineta. Debe ser único en toda la flota. */
    @Column(name = "serial_code", nullable = false, unique = true, length = 50)
    private String serialCode;

    /** Modelo comercial de la patineta (ej. "Xiaomi Pro 2"). */
    @Column(nullable = false, length = 80)
    private String model;

    /** Nivel de batería actual en porcentaje. Rango válido: 0–100. */
    @Column(nullable = false)
    private Integer battery;

    /** Ubicación física actual de la patineta (ej. "Plaza Italia, Santiago"). */
    @Column(nullable = false, length = 100)
    private String location;

    /** Estado operativo de la patineta. Por defecto AVAILABLE al registrarse. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScooterStatus status = ScooterStatus.AVAILABLE;

    /** Fecha y hora de registro de la patineta en el sistema. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
