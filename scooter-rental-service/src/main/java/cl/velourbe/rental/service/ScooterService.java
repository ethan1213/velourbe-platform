package cl.velourbe.rental.service;

import cl.velourbe.rental.exception.*;
import cl.velourbe.rental.model.dto.*;
import cl.velourbe.rental.model.entity.Scooter;
import cl.velourbe.rental.model.enums.ScooterStatus;
import cl.velourbe.rental.repository.ScooterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Servicio de lógica de negocio para la gestión del inventario de patinetas.
 * Coordina el acceso a {@link ScooterRepository} y transforma entidades en DTOs.
 */
@Service
@RequiredArgsConstructor
public class ScooterService {

    private final ScooterRepository scooterRepository;

    /**
     * Retorna la lista completa de patinetas registradas en el sistema.
     *
     * @return lista de {@link ScooterResponseDTO} con todas las patinetas
     */
    public List<ScooterResponseDTO> findAll() {
        return scooterRepository.findAll().stream().map(this::toDTO).toList();
    }

    /**
     * Retorna solo las patinetas con estado {@code AVAILABLE}.
     *
     * @return lista de patinetas disponibles para arrendar
     */
    public List<ScooterResponseDTO> findAvailable() {
        return scooterRepository.findByStatus(ScooterStatus.AVAILABLE)
                .stream().map(this::toDTO).toList();
    }

    /**
     * Registra una nueva patineta en el sistema con estado inicial {@code AVAILABLE}.
     *
     * @param dto datos de la patineta a crear
     * @return DTO de la patineta creada con su ID asignado
     */
    public ScooterResponseDTO create(ScooterRequestDTO dto) {
        Scooter s = new Scooter();
        s.setSerialCode(dto.getSerialCode());
        s.setModel(dto.getModel());
        s.setBattery(dto.getBattery());
        s.setLocation(dto.getLocation());
        return toDTO(scooterRepository.save(s));
    }

    /**
     * Busca una patineta por su identificador único.
     *
     * @param id identificador de la patineta
     * @return DTO de la patineta encontrada
     * @throws ScooterNotFoundException si no existe ninguna patineta con ese ID
     */
    public ScooterResponseDTO findById(Long id) {
        return toDTO(scooterRepository.findById(id)
                .orElseThrow(() -> new ScooterNotFoundException(id)));
    }

    /**
     * Elimina una patineta del sistema de forma permanente.
     *
     * @param id identificador de la patineta a eliminar
     * @throws ScooterNotFoundException si no existe ninguna patineta con ese ID
     */
    public void delete(Long id) {
        if (!scooterRepository.existsById(id)) throw new ScooterNotFoundException(id);
        scooterRepository.deleteById(id);
    }

    /**
     * Retorna patinetas con nivel de batería inferior al umbral indicado,
     * ordenadas de menor a mayor batería.
     * Usa la consulta JPQL personalizada {@code findByBatteryBelow} del repositorio.
     *
     * @param threshold porcentaje máximo de batería (exclusivo, 0-100)
     * @return lista de patinetas con batería crítica ordenadas ascendentemente
     */
    public List<ScooterResponseDTO> findLowBattery(int threshold) {
        return scooterRepository.findByBatteryBelow(threshold)
                .stream().map(this::toDTO).toList();
    }

    /**
     * Busca patinetas cuya ubicación contenga el texto dado (case-insensitive).
     * Usa la consulta JPQL personalizada {@code findByLocationContaining} del repositorio.
     *
     * @param location texto parcial de ubicación a buscar
     * @return lista de patinetas cuya ubicación coincide con el patrón
     */
    public List<ScooterResponseDTO> searchByLocation(String location) {
        return scooterRepository.findByLocationContaining(location)
                .stream().map(this::toDTO).toList();
    }

    /**
     * Convierte una entidad {@link Scooter} en su representación DTO de respuesta.
     *
     * @param s entidad de patineta
     * @return DTO con los datos de la patineta
     */
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
