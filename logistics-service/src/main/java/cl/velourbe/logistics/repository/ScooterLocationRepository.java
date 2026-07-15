package cl.velourbe.logistics.repository;

import cl.velourbe.logistics.model.entity.ScooterLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ScooterLocation entity operations.
 * Handles data access layer for scooter locations using Spring Data JPA.
 */
@Repository
public interface ScooterLocationRepository extends JpaRepository<ScooterLocation, Long> {
    
    /**
     * Finds a scooter location by scooter ID.
     * 
     * @param scooterId the scooter ID
     * @return optional containing the location if found
     */
    Optional<ScooterLocation> findByScooterId(Long scooterId);
    
    /**
     * Finds all scooters within a geographic area using haversine formula.
     * Returns scooters within specified radius from given latitude/longitude.
     * 
     * @param latitude center latitude
     * @param longitude center longitude
     * @param radiusKm radius in kilometers
     * @return list of scooter locations within the area
     */
    @Query(value = """
        SELECT * FROM scooter_locations sl WHERE
        (6371 * ACOS(COS(RADIANS(:latitude)) * COS(RADIANS(sl.latitude)) *
        COS(RADIANS(sl.longitude) - RADIANS(:longitude)) +
        SIN(RADIANS(:latitude)) * SIN(RADIANS(sl.latitude)))) <= :radiusKm
        """, nativeQuery = true)
    List<ScooterLocation> findScootersInArea(
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude,
        @Param("radiusKm") Double radiusKm
    );
}
