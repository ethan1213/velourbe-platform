package cl.velourbe.rental.repository;

import cl.velourbe.rental.model.entity.Scooter;
import cl.velourbe.rental.model.enums.ScooterStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScooterRepository extends JpaRepository<Scooter, Long> {
    List<Scooter> findByStatus(ScooterStatus status);
}
