package cl.velourbe.rental.service;

import cl.velourbe.rental.exception.RentalNotFoundException;
import cl.velourbe.rental.exception.ScooterNotAvailableException;
import cl.velourbe.rental.exception.ScooterNotFoundException;
import cl.velourbe.rental.model.dto.RentalRequestDTO;
import cl.velourbe.rental.model.dto.RentalResponseDTO;
import cl.velourbe.rental.model.entity.Rental;
import cl.velourbe.rental.model.entity.Scooter;
import cl.velourbe.rental.model.enums.RentalStatus;
import cl.velourbe.rental.model.enums.ScooterStatus;
import cl.velourbe.rental.repository.RentalRepository;
import cl.velourbe.rental.repository.ScooterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import cl.velourbe.rental.security.SecurityUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock RentalRepository rentalRepository;
    @Mock ScooterRepository scooterRepository;
    @InjectMocks RentalService rentalService;

    private Scooter availableScooter;
    private Scooter inUseScooter;

    @BeforeEach
    void setUp() {
        availableScooter = new Scooter();
        availableScooter.setId(1L);
        availableScooter.setSerialCode("SC-001");
        availableScooter.setModel("Xiaomi Pro 2");
        availableScooter.setBattery(80);
        availableScooter.setLocation("Plaza Italia");
        availableScooter.setStatus(ScooterStatus.AVAILABLE);
        availableScooter.setCreatedAt(LocalDateTime.now());

        inUseScooter = new Scooter();
        inUseScooter.setId(2L);
        inUseScooter.setSerialCode("SC-002");
        inUseScooter.setModel("Segway E2");
        inUseScooter.setBattery(60);
        inUseScooter.setLocation("Bellavista");
        inUseScooter.setStatus(ScooterStatus.IN_USE);
        inUseScooter.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void startRental_exitoso_creaArriendo() {
        RentalRequestDTO dto = new RentalRequestDTO();
        dto.setScooterId(1L);

        Rental savedRental = new Rental();
        savedRental.setId(10L);
        savedRental.setUserId(5L);
        savedRental.setScooter(availableScooter);
        savedRental.setStartedAt(LocalDateTime.now());
        savedRental.setStatus(RentalStatus.ACTIVE);

        when(scooterRepository.findById(1L)).thenReturn(Optional.of(availableScooter));
        when(scooterRepository.save(any(Scooter.class))).thenReturn(availableScooter);
        when(rentalRepository.save(any(Rental.class))).thenReturn(savedRental);

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(5L);

            RentalResponseDTO result = rentalService.startRental(dto);

            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getStatus()).isEqualTo("ACTIVE");
            verify(scooterRepository).save(argThat(s -> s.getStatus() == ScooterStatus.IN_USE));
        }
    }

    @Test
    void startRental_patinSetaInexistente_lanzaExcepcion() {
        RentalRequestDTO dto = new RentalRequestDTO();
        dto.setScooterId(99L);

        when(scooterRepository.findById(99L)).thenReturn(Optional.empty());

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(5L);

            assertThatThrownBy(() -> rentalService.startRental(dto))
                    .isInstanceOf(ScooterNotFoundException.class);
        }
    }

    @Test
    void startRental_patinSetaNoDisponible_lanzaExcepcion() {
        RentalRequestDTO dto = new RentalRequestDTO();
        dto.setScooterId(2L);

        when(scooterRepository.findById(2L)).thenReturn(Optional.of(inUseScooter));

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(5L);

            assertThatThrownBy(() -> rentalService.startRental(dto))
                    .isInstanceOf(ScooterNotAvailableException.class);
        }
    }

    @Test
    void endRental_exitoso_calculaMinutos() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(45);
        Rental activeRental = new Rental();
        activeRental.setId(10L);
        activeRental.setUserId(5L);
        activeRental.setScooter(availableScooter);
        activeRental.setStartedAt(start);
        activeRental.setStatus(RentalStatus.ACTIVE);

        Rental completedRental = new Rental();
        completedRental.setId(10L);
        completedRental.setUserId(5L);
        completedRental.setScooter(availableScooter);
        completedRental.setStartedAt(start);
        completedRental.setEndedAt(LocalDateTime.now());
        completedRental.setStatus(RentalStatus.COMPLETED);
        completedRental.setTotalMinutes(45);

        when(rentalRepository.findById(10L)).thenReturn(Optional.of(activeRental));
        when(scooterRepository.save(any(Scooter.class))).thenReturn(availableScooter);
        when(rentalRepository.save(any(Rental.class))).thenReturn(completedRental);

        RentalResponseDTO result = rentalService.endRental(10L);

        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(result.getTotalMinutes()).isEqualTo(45);
    }

    @Test
    void endRental_arriendoInexistente_lanzaExcepcion() {
        when(rentalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalService.endRental(99L))
                .isInstanceOf(RentalNotFoundException.class);
    }

    @Test
    void myRentals_retornaArriendosDelUsuario() {
        Rental r = new Rental();
        r.setId(1L);
        r.setUserId(5L);
        r.setScooter(availableScooter);
        r.setStartedAt(LocalDateTime.now().minusMinutes(30));
        r.setStatus(RentalStatus.COMPLETED);
        r.setTotalMinutes(30);

        when(rentalRepository.findByUserId(5L)).thenReturn(List.of(r));

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(5L);

            List<RentalResponseDTO> result = rentalService.myRentals();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUserId()).isEqualTo(5L);
        }
    }
}
