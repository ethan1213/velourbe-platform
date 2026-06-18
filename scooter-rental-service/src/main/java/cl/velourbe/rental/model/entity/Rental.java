package cl.velourbe.rental.model.entity;

import cl.velourbe.rental.model.enums.RentalStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un arriendo de patineta.
 * Se mapea a la tabla {@code rentals} de la base de datos {@code db_scooter_rentals}.
 * El campo {@code userId} referencia al usuario del user-auth-service (sin FK entre bases de datos).
 */
@Data
@Entity
@Table(name = "rentals")
public class Rental {

    /** Identificador único autoincremental. */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID del usuario que realizó el arriendo, extraído del token JWT. Sin FK a otra base de datos. */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Patineta arrendada. Cargada de forma lazy para evitar consultas innecesarias. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scooter_id", nullable = false)
    private Scooter scooter;

    /** Momento exacto en que se inició el arriendo. */
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    /** Momento exacto en que se finalizó el arriendo. Null mientras esté activo. */
    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    /** Estado del arriendo. ACTIVE mientras corre, COMPLETED al finalizar. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RentalStatus status = RentalStatus.ACTIVE;

    /** Duración total calculada en minutos al finalizar el arriendo. Null si aún está activo. */
    @Column(name = "total_minutes")
    private Integer totalMinutes;
}
