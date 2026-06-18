package cl.velourbe.rental.service;

import cl.velourbe.rental.exception.*;
import cl.velourbe.rental.model.dto.*;
import cl.velourbe.rental.model.entity.Scooter;
import cl.velourbe.rental.model.enums.ScooterStatus;
import cl.velourbe.rental.repository.ScooterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScooterService {

    private final ScooterRepository scooterRepository;

    public List<ScooterResponseDTO> findAll() {
        log.debug("Listando todas las patinetas");
        return scooterRepository.findAll().stream().map(this::toDTO).toList();
    }

    public List<ScooterResponseDTO> findAvailable() {
        log.debug("Listando patinetas disponibles");
        return scooterRepository.findByStatus(ScooterStatus.AVAILABLE)
                .stream().map(this::toDTO).toList();
    }

    public ScooterResponseDTO create(ScooterRequestDTO dto) {
        log.info("Creando patineta serialCode={}", dto.getSerialCode());
        Scooter s = new Scooter();
        s.setSerialCode(dto.getSerialCode());
        s.setModel(dto.getModel());
        s.setBattery(dto.getBattery());
        s.setLocation(dto.getLocation());
        ScooterResponseDTO created = toDTO(scooterRepository.save(s));
        log.info("Patineta creada id={}", created.getId());
        return created;
    }

    public ScooterResponseDTO findById(Long id) {
        log.debug("Buscando patineta id={}", id);
        return toDTO(scooterRepository.findById(id)
                .orElseThrow(() -> new ScooterNotFoundException(id)));
    }

    public void delete(Long id) {
        log.info("Eliminando patineta id={}", id);
        if (!scooterRepository.existsById(id)) throw new ScooterNotFoundException(id);
        scooterRepository.deleteById(id);
        log.info("Patineta id={} eliminada", id);
    }

    public List<ScooterResponseDTO> findLowBattery(int threshold) {
        log.debug("Buscando patinetas con batería < {}", threshold);
        return scooterRepository.findByBatteryBelow(threshold)
                .stream().map(this::toDTO).toList();
    }

    public List<ScooterResponseDTO> searchByLocation(String location) {
        log.debug("Buscando patinetas por ubicación: {}", location);
        return scooterRepository.findByLocationContaining(location)
                .stream().map(this::toDTO).toList();
    }

    private ScooterResponseDTO toDTO(Scooter s) {
        ScooterResponseDTO dto = new ScooterResponseDTO();
        dto.setId(s.getId());
        dto.setSerialCode(s.getSerialCode());
        dto.setModel(s.getModel());
        dto.setBattery(s.getBattery());
        dto.setLocation(s.getLocation());
        dto.setStatus(s.getStatus().name());
        dto.setCreatedAt(s.getCreatedAt());
        return dto;
    }
}
