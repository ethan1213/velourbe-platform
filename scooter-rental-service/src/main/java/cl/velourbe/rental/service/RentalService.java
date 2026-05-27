package cl.velourbe.rental.service;

import cl.velourbe.rental.exception.*;
import cl.velourbe.rental.model.dto.*;
import cl.velourbe.rental.model.entity.*;
import cl.velourbe.rental.model.enums.*;
import cl.velourbe.rental.repository.*;
import cl.velourbe.rental.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;

/**
 * Servicio de lógica de negocio para el ciclo de vida de los arriendos.
 * Gestiona el inicio, finalización y consulta de arriendos, actualizando
 * el estado de las patinetas involucradas de forma transaccional.
 */
@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final ScooterRepository scooterRepository;

    /**
     * Inicia un nuevo arriendo para el usuario autenticado.
     * Cambia el estado de la patineta a {@code IN_USE} y crea el registro de arriendo.
     * Operación transaccional: si algo falla, ningún cambio persiste.
     *
     * @param dto contiene el ID de la patineta a arrendar
     * @return DTO del arriendo recién creado con estado ACTIVE
     * @throws ScooterNotFoundException     si la patineta no existe
     * @throws ScooterNotAvailableException si la patineta no está disponible
     */
    @Transactional
    public RentalResponseDTO startRental(RentalRequestDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        Scooter scooter = scooterRepository.findById(dto.getScooterId())
                .orElseThrow(() -> new ScooterNotFoundException(dto.getScooterId()));
        if (scooter.getStatus() != ScooterStatus.AVAILABLE) {
            throw new ScooterNotAvailableException(scooter.getId());
        }
        scooter.setStatus(ScooterStatus.IN_USE);
        scooterRepository.save(scooter);

        Rental rental = new Rental();
        rental.setUserId(userId);
        rental.setScooter(scooter);
        rental.setStartedAt(LocalDateTime.now());
        return toDTO(rentalRepository.save(rental));
    }

    /**
     * Finaliza un arriendo activo, calcula la duración total en minutos
     * y devuelve la patineta al estado {@code AVAILABLE}.
     * Operación transaccional: si algo falla, ningún cambio persiste.
     *
     * @param rentalId identificador del arriendo a finalizar
     * @return DTO del arriendo actualizado con estado COMPLETED y duración calculada
     * @throws RentalNotFoundException si no existe el arriendo con ese ID
     */
    @Transactional
    public RentalResponseDTO endRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RentalNotFoundException(rentalId));
        rental.setEndedAt(LocalDateTime.now());
        rental.setStatus(RentalStatus.COMPLETED);
        long minutes = Duration.between(rental.getStartedAt(), rental.getEndedAt()).toMinutes();
        rental.setTotalMinutes((int) minutes);

        Scooter scooter = rental.getScooter();
        scooter.setStatus(ScooterStatus.AVAILABLE);
        scooterRepository.save(scooter);

        return toDTO(rentalRepository.save(rental));
    }

    /**
     * Retorna el historial completo de arriendos del usuario autenticado.
     *
     * @return lista de DTOs con todos los arriendos del usuario actual
     */
    public List<RentalResponseDTO> myRentals() {
        Long userId = SecurityUtils.getCurrentUserId();
        return rentalRepository.findByUserId(userId).stream().map(this::toDTO).toList();
    }

    /**
     * Retorna arriendos completados cuya duración supera el mínimo indicado.
     * Usa la consulta JPQL personalizada {@code findCompletedWithMinDuration} del repositorio.
     *
     * @param minMinutes duración mínima en minutos (inclusivo)
     * @return lista de DTOs de arriendos largos completados
     */
    public List<RentalResponseDTO> findLongRentals(int minMinutes) {
        return rentalRepository.findCompletedWithMinDuration(minMinutes)
                .stream().map(this::toDTO).toList();
    }

    /**
     * Convierte una entidad {@link Rental} en su representación DTO de respuesta.
     *
     * @param r entidad de arriendo
     * @return DTO con los datos del arriendo
     */
    private RentalResponseDTO toDTO(Rental r) {
        RentalResponseDTO dto = new RentalResponseDTO();
        dto.setId(r.getId());
        dto.setUserId(r.getUserId());
        dto.setScooterId(r.getScooter().getId());
        dto.setScooterModel(r.getScooter().getModel());
        dto.setStartedAt(r.getStartedAt());
        dto.setEndedAt(r.getEndedAt());
        dto.setStatus(r.getStatus().name());
        dto.setTotalMinutes(r.getTotalMinutes());
        return dto;
    }
}
