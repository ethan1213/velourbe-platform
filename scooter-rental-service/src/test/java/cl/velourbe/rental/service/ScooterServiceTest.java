package cl.velourbe.rental.service;

import cl.velourbe.rental.exception.ScooterNotFoundException;
import cl.velourbe.rental.model.dto.ScooterRequestDTO;
import cl.velourbe.rental.model.dto.ScooterResponseDTO;
import cl.velourbe.rental.model.entity.Scooter;
import cl.velourbe.rental.model.enums.ScooterStatus;
import cl.velourbe.rental.repository.ScooterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScooterServiceTest {

    @Mock ScooterRepository scooterRepository;
    @InjectMocks ScooterService scooterService;

    private Scooter buildScooter(Long id, String serial, ScooterStatus status) {
        Scooter s = new Scooter();
        s.setId(id);
        s.setSerialCode(serial);
        s.setModel("Xiaomi Pro 2");
        s.setBattery(80);
        s.setLocation("Plaza Italia");
        s.setStatus(status);
        s.setCreatedAt(LocalDateTime.now());
        return s;
    }

    @Test
    void findAll_retornaTodasLasPatinetas() {
        when(scooterRepository.findAll()).thenReturn(List.of(
                buildScooter(1L, "SC-001", ScooterStatus.AVAILABLE),
                buildScooter(2L, "SC-002", ScooterStatus.IN_USE)
        ));

        List<ScooterResponseDTO> result = scooterService.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void findAvailable_retornaSoloDisponibles() {
        when(scooterRepository.findByStatus(ScooterStatus.AVAILABLE)).thenReturn(List.of(
                buildScooter(1L, "SC-001", ScooterStatus.AVAILABLE)
        ));

        List<ScooterResponseDTO> result = scooterService.findAvailable();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    void create_guardaYRetornaDTO() {
        ScooterRequestDTO dto = new ScooterRequestDTO();
        dto.setSerialCode("SC-NEW");
        dto.setModel("Segway E2");
        dto.setBattery(95);
        dto.setLocation("Providencia");

        Scooter saved = buildScooter(10L, "SC-NEW", ScooterStatus.AVAILABLE);
        when(scooterRepository.save(any(Scooter.class))).thenReturn(saved);

        ScooterResponseDTO result = scooterService.create(dto);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getSerialCode()).isEqualTo("SC-NEW");
        verify(scooterRepository).save(any(Scooter.class));
    }

    @Test
    void findById_existente_retornaDTO() {
        Scooter s = buildScooter(3L, "SC-003", ScooterStatus.AVAILABLE);
        when(scooterRepository.findById(3L)).thenReturn(Optional.of(s));

        ScooterResponseDTO result = scooterService.findById(3L);

        assertThat(result.getId()).isEqualTo(3L);
    }

    @Test
    void findById_inexistente_lanzaExcepcion() {
        when(scooterRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scooterService.findById(99L))
                .isInstanceOf(ScooterNotFoundException.class);
    }

    @Test
    void delete_existente_eliminaPatineta() {
        when(scooterRepository.existsById(5L)).thenReturn(true);

        scooterService.delete(5L);

        verify(scooterRepository).deleteById(5L);
    }

    @Test
    void delete_inexistente_lanzaExcepcion() {
        when(scooterRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> scooterService.delete(99L))
                .isInstanceOf(ScooterNotFoundException.class);
        verify(scooterRepository, never()).deleteById(any());
    }
}
