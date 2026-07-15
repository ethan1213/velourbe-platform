package cl.velourbe.logistics.service;

import cl.velourbe.logistics.exception.LocationNotFoundException;
import cl.velourbe.logistics.model.dto.LocationDTO;
import cl.velourbe.logistics.model.entity.ScooterLocation;
import cl.velourbe.logistics.repository.ScooterLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for logistics and location operations.
 * Handles all business logic for scooter location tracking and geographic queries.
 * All public methods include comprehensive Javadoc as per architecture requirements.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsService {
    
    private final ScooterLocationRepository scooterLocationRepository;
    
    /**
     * Records or updates the location of a scooter.
     * 
     * Updates the scooter's current location with provided latitude and longitude.
     * If the scooter location record doesn't exist, creates a new one.
     * If it exists, updates the coordinates and timestamp.
     * 
     * @param scooterId the unique identifier for the scooter
     * @param latitude the latitude coordinate (range: -90 to 90)
     * @param longitude the longitude coordinate (range: -180 to 180)
     * @return the updated location DTO with all fields populated
     * @throws IllegalArgumentException if coordinates are invalid
     */
    @Transactional
    public LocationDTO recordScooterLocation(Long scooterId, Double latitude, Double longitude) {
        log.info("Recording location for scooter={} lat={} lon={}", scooterId, latitude, longitude);
        
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Coordenadas inválidas: latitud [-90, 90], longitud [-180, 180]");
        }
        
        ScooterLocation location = scooterLocationRepository.findByScooterId(scooterId)
            .orElse(ScooterLocation.builder().scooterId(scooterId).build());
        
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        
        location = scooterLocationRepository.save(location);
        log.debug("Location recorded for scooter id={}", scooterId);
        
        return toDTO(location);
    }
    
    /**
     * Retrieves the current location of a scooter.
     * 
     * Returns the latest recorded location for the specified scooter.
     * Useful for real-time tracking and map display in mobile/web apps.
     * 
     * @param scooterId the unique identifier for the scooter
     * @return the location DTO containing latitude, longitude, and timestamps
     * @throws LocationNotFoundException if scooter location record does not exist
     */
    public LocationDTO getScooterLocation(Long scooterId) {
        log.debug("Fetching location for scooter={}", scooterId);
        
        return scooterLocationRepository.findByScooterId(scooterId)
            .map(this::toDTO)
            .orElseThrow(() -> {
                log.warn("Scooter location not found for scooterId={}", scooterId);
                return new LocationNotFoundException(scooterId);
            });
    }
    
    /**
     * Finds all scooters within a geographic area.
     * 
     * Uses haversine formula to calculate distances on Earth's surface.
     * Returns all scooters within the specified radius from the center point.
     * Useful for finding available scooters near user location for rentals.
     * 
     * @param latitude the center latitude coordinate
     * @param longitude the center longitude coordinate
     * @param radiusKm the search radius in kilometers
     * @return list of location DTOs for scooters in the area, ordered by discovery order
     * @throws IllegalArgumentException if coordinates or radius are invalid
     */
    public List<LocationDTO> getScootersInArea(Double latitude, Double longitude, Double radiusKm) {
        log.debug("Finding scooters in area: lat={} lon={} radius={}km", latitude, longitude, radiusKm);
        
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Coordenadas inválidas: latitud [-90, 90], longitud [-180, 180]");
        }
        
        if (radiusKm <= 0) {
            throw new IllegalArgumentException("El radio debe ser mayor a 0");
        }
        
        List<ScooterLocation> locations = scooterLocationRepository.findScootersInArea(latitude, longitude, radiusKm);
        log.info("Found {} scooters in area", locations.size());
        
        return locations.stream().map(this::toDTO).toList();
    }
    
    /**
     * Converts a ScooterLocation entity to a LocationDTO.
     * 
     * @param location the scooter location entity to convert
     * @return the location DTO with all fields populated
     */
    private LocationDTO toDTO(ScooterLocation location) {
        return new LocationDTO(
            location.getId(),
            location.getScooterId(),
            location.getLatitude(),
            location.getLongitude(),
            location.getCreatedAt(),
            location.getUpdatedAt()
        );
    }
}
