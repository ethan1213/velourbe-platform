package cl.velourbe.rental.repository;

import cl.velourbe.rental.model.entity.Rental;
import cl.velourbe.rental.model.enums.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByUserId(Long userId);
    Optional<Rental> findByUserIdAndStatus(Long userId, RentalStatus status);
}
