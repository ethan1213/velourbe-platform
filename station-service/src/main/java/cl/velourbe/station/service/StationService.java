package cl.velourbe.station.service;

import cl.velourbe.station.exception.InvalidStationException;
import cl.velourbe.station.exception.StationNotFoundException;
import cl.velourbe.station.model.dto.CreateStationRequestDTO;
import cl.velourbe.station.model.dto.StationResponseDTO;
import cl.velourbe.station.model.entity.Station;
import cl.velourbe.station.model.enums.StationStatus;
import cl.velourbe.station.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Service layer for station operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StationService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final StationRepository repository;

    /**
     * Creates a new station.
     */
    @Transactional
    public StationResponseDTO create(CreateStationRequestDTO dto) {
        log.info("Creando estación name='{}' capacity={}", dto.name(), dto.capacity());
        Station s = Station.builder()
                .name(dto.name())
                .address(dto.address())
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .capacity(dto.capacity())
                .occupied(0)
                .status(StationStatus.ACTIVE)
                .build();
        return toDTO(repository.save(s));
    }

    /**
     * Returns a station by id, or throws if not found.
     */
    public StationResponseDTO getById(Long id) {
        return toDTO(mustFind(id));
    }

    /**
     * Returns all stations ordered by name.
     */
    public List<StationResponseDTO> getAll() {
        return repository.findAllByOrderByNameAsc().stream().map(this::toDTO).toList();
    }

    /**
     * Returns active stations within the given radius (km) of a coordinate,
     * ordered by distance (Haversine formula).
     */
    public List<StationResponseDTO> getNearby(double latitude, double longitude, double radiusKm) {
        log.info("Buscando estaciones cerca de ({}, {}) radio={}km", latitude, longitude, radiusKm);
        return repository.findByStatusOrderByNameAsc(StationStatus.ACTIVE).stream()
                .filter(s -> distanceKm(latitude, longitude, s.getLatitude(), s.getLongitude()) <= radiusKm)
                .sorted(Comparator.comparingDouble(
                        s -> distanceKm(latitude, longitude, s.getLatitude(), s.getLongitude())))
                .map(this::toDTO)
                .toList();
    }

    /**
     * Docks a scooter at the station (occupied + 1). Marks the station FULL when it reaches capacity.
     */
    @Transactional
    public StationResponseDTO dock(Long id) {
        Station s = mustFind(id);
        if (s.getStatus() == StationStatus.INACTIVE || s.getStatus() == StationStatus.MAINTENANCE) {
            throw new InvalidStationException("La estación no está operativa");
        }
        if (s.getOccupied() >= s.getCapacity()) {
            throw new InvalidStationException("La estación está llena");
        }
        s.setOccupied(s.getOccupied() + 1);
        if (s.getOccupied().equals(s.getCapacity())) {
            s.setStatus(StationStatus.FULL);
        }
        log.info("Dock en estación {} — ocupación {}/{}", id, s.getOccupied(), s.getCapacity());
        return toDTO(repository.save(s));
    }

    /**
     * Undocks a scooter from the station (occupied - 1). Reactivates a FULL station.
     */
    @Transactional
    public StationResponseDTO undock(Long id) {
        Station s = mustFind(id);
        if (s.getOccupied() <= 0) {
            throw new InvalidStationException("La estación está vacía");
        }
        s.setOccupied(s.getOccupied() - 1);
        if (s.getStatus() == StationStatus.FULL) {
            s.setStatus(StationStatus.ACTIVE);
        }
        log.info("Undock en estación {} — ocupación {}/{}", id, s.getOccupied(), s.getCapacity());
        return toDTO(repository.save(s));
    }

    /**
     * Puts a station into maintenance mode.
     */
    @Transactional
    public StationResponseDTO setMaintenance(Long id) {
        Station s = mustFind(id);
        s.setStatus(StationStatus.MAINTENANCE);
        log.info("Estación {} en mantención", id);
        return toDTO(repository.save(s));
    }

    /**
     * Reactivates a station (back to ACTIVE, or FULL if at capacity).
     */
    @Transactional
    public StationResponseDTO activate(Long id) {
        Station s = mustFind(id);
        s.setStatus(s.getOccupied() >= s.getCapacity() ? StationStatus.FULL : StationStatus.ACTIVE);
        log.info("Estación {} reactivada", id);
        return toDTO(repository.save(s));
    }

    /**
     * Deletes a station.
     */
    @Transactional
    public void delete(Long id) {
        Station s = mustFind(id);
        repository.delete(s);
        log.info("Estación {} eliminada", id);
    }

    private Station mustFind(Long id) {
        return repository.findById(id).orElseThrow(() -> new StationNotFoundException(id));
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private StationResponseDTO toDTO(Station s) {
        return new StationResponseDTO(
                s.getId(), s.getName(), s.getAddress(),
                s.getLatitude(), s.getLongitude(),
                s.getCapacity(), s.getOccupied(),
                s.getCapacity() - s.getOccupied(),
                s.getStatus() != null ? s.getStatus().name() : null,
                s.getCreatedAt(), s.getUpdatedAt());
    }
}
