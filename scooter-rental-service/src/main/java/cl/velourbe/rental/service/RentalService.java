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

@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final ScooterRepository scooterRepository;

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

    public List<RentalResponseDTO> myRentals() {
        Long userId = SecurityUtils.getCurrentUserId();
        return rentalRepository.findByUserId(userId).stream().map(this::toDTO).toList();
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
