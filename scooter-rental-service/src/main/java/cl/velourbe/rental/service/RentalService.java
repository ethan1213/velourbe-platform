package cl.velourbe.rental.service;

import cl.velourbe.rental.exception.*;
import cl.velourbe.rental.model.dto.*;
import cl.velourbe.rental.model.entity.*;
import cl.velourbe.rental.model.enums.*;
import cl.velourbe.rental.repository.*;
import cl.velourbe.rental.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final ScooterRepository scooterRepository;

    @Transactional
    public RentalResponseDTO startRental(RentalRequestDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Iniciando arriendo scooterId={} userId={}", dto.getScooterId(), userId);
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
        RentalResponseDTO result = toDTO(rentalRepository.save(rental));
        log.info("Arriendo id={} iniciado", result.getId());
        return result;
    }

    @Transactional
    public RentalResponseDTO endRental(Long rentalId) {
        log.info("Finalizando arriendo id={}", rentalId);
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RentalNotFoundException(rentalId));
        rental.setEndedAt(LocalDateTime.now());
        rental.setStatus(RentalStatus.COMPLETED);
        long minutes = Duration.between(rental.getStartedAt(), rental.getEndedAt()).toMinutes();
        rental.setTotalMinutes((int) minutes);

        Scooter scooter = rental.getScooter();
        scooter.setStatus(ScooterStatus.AVAILABLE);
        scooterRepository.save(scooter);

        RentalResponseDTO result = toDTO(rentalRepository.save(rental));
        log.info("Arriendo id={} finalizado en {} minutos", rentalId, minutes);
        return result;
    }

    public List<RentalResponseDTO> myRentals() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.debug("Listando arriendos de userId={}", userId);
        return rentalRepository.findByUserId(userId).stream().map(this::toDTO).toList();
    }

    public List<RentalResponseDTO> findLongRentals(int minMinutes) {
        log.debug("Listando arriendos con duración >= {} min", minMinutes);
        return rentalRepository.findCompletedWithMinDuration(minMinutes)
                .stream().map(this::toDTO).toList();
    }

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
