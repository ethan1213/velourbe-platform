package cl.velourbe.rental.service;

import cl.velourbe.rental.exception.*;
import cl.velourbe.rental.model.dto.*;
import cl.velourbe.rental.model.entity.Scooter;
import cl.velourbe.rental.model.enums.ScooterStatus;
import cl.velourbe.rental.repository.ScooterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScooterService {

    private final ScooterRepository scooterRepository;

    public List<ScooterResponseDTO> findAll() {
        return scooterRepository.findAll().stream().map(this::toDTO).toList();
    }

    public List<ScooterResponseDTO> findAvailable() {
        return scooterRepository.findByStatus(ScooterStatus.AVAILABLE)
                .stream().map(this::toDTO).toList();
    }

    public ScooterResponseDTO create(ScooterRequestDTO dto) {
        Scooter s = new Scooter();
        s.setSerialCode(dto.getSerialCode());
        s.setModel(dto.getModel());
        s.setBattery(dto.getBattery());
        s.setLocation(dto.getLocation());
        return toDTO(scooterRepository.save(s));
    }

    public ScooterResponseDTO findById(Long id) {
        return toDTO(scooterRepository.findById(id)
                .orElseThrow(() -> new ScooterNotFoundException(id)));
    }

    public void delete(Long id) {
        if (!scooterRepository.existsById(id)) throw new ScooterNotFoundException(id);
        scooterRepository.deleteById(id);
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
