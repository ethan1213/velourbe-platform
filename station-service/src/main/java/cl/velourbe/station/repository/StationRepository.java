package cl.velourbe.station.repository;

import cl.velourbe.station.model.entity.Station;
import cl.velourbe.station.model.enums.StationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Station operations.
 */
@Repository
public interface StationRepository extends JpaRepository<Station, Long> {

    /**
     * Finds all stations with a given status.
     */
    List<Station> findByStatusOrderByNameAsc(StationStatus status);

    /**
     * Returns all stations ordered by name.
     */
    List<Station> findAllByOrderByNameAsc();
}
