package cl.velourbe.station.service;

import cl.velourbe.station.exception.InvalidStationException;
import cl.velourbe.station.exception.StationNotFoundException;
import cl.velourbe.station.model.dto.CreateStationRequestDTO;
import cl.velourbe.station.model.dto.StationResponseDTO;
import cl.velourbe.station.model.entity.Station;
import cl.velourbe.station.model.enums.StationStatus;
import cl.velourbe.station.repository.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {

    @Mock
    private StationRepository repository;

    @InjectMocks
    private StationService service;

    private Station station;

    @BeforeEach
    void setUp() {
        station = Station.builder()
                .id(1L)
                .name("Estación Plaza Italia")
                .address("Av. Providencia 1")
                .latitude(-33.4372)
                .longitude(-70.6344)
                .capacity(10)
                .occupied(3)
                .status(StationStatus.ACTIVE)
                .build();
    }

    @Test
    void create_deberiaGuardarEstacionNueva() {
        when(repository.save(any(Station.class))).thenAnswer(inv -> inv.getArgument(0));

        StationResponseDTO dto = service.create(new CreateStationRequestDTO(
                "Estación Plaza Italia", "Av. Providencia 1", -33.4372, -70.6344, 10));

        assertThat(dto.name()).isEqualTo("Estación Plaza Italia");
        assertThat(dto.occupied()).isZero();
        assertThat(dto.status()).isEqualTo("ACTIVE");
    }

    @Test
    void getById_deberiaRetornarEstacionExistente() {
        when(repository.findById(1L)).thenReturn(Optional.of(station));

        StationResponseDTO dto = service.getById(1L);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.availableSlots()).isEqualTo(7);
    }

    @Test
    void getById_deberiaLanzarSiNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(StationNotFoundException.class);
    }

    @Test
    void dock_deberiaIncrementarOcupacion() {
        when(repository.findById(1L)).thenReturn(Optional.of(station));
        when(repository.save(any(Station.class))).thenAnswer(inv -> inv.getArgument(0));

        StationResponseDTO dto = service.dock(1L);

        assertThat(dto.occupied()).isEqualTo(4);
        assertThat(dto.status()).isEqualTo("ACTIVE");
    }

    @Test
    void dock_deberiaMarcarFullAlLlenarse() {
        station.setOccupied(9);
        when(repository.findById(1L)).thenReturn(Optional.of(station));
        when(repository.save(any(Station.class))).thenAnswer(inv -> inv.getArgument(0));

        StationResponseDTO dto = service.dock(1L);

        assertThat(dto.occupied()).isEqualTo(10);
        assertThat(dto.status()).isEqualTo("FULL");
    }

    @Test
    void dock_deberiaFallarSiEstacionLlena() {
        station.setOccupied(10);
        when(repository.findById(1L)).thenReturn(Optional.of(station));

        assertThatThrownBy(() -> service.dock(1L))
                .isInstanceOf(InvalidStationException.class);
    }

    @Test
    void undock_deberiaDecrementarYReactivar() {
        station.setOccupied(10);
        station.setStatus(StationStatus.FULL);
        when(repository.findById(1L)).thenReturn(Optional.of(station));
        when(repository.save(any(Station.class))).thenAnswer(inv -> inv.getArgument(0));

        StationResponseDTO dto = service.undock(1L);

        assertThat(dto.occupied()).isEqualTo(9);
        assertThat(dto.status()).isEqualTo("ACTIVE");
    }

    @Test
    void undock_deberiaFallarSiEstacionVacia() {
        station.setOccupied(0);
        when(repository.findById(1L)).thenReturn(Optional.of(station));

        assertThatThrownBy(() -> service.undock(1L))
                .isInstanceOf(InvalidStationException.class);
    }

    @Test
    void getNearby_deberiaFiltrarPorRadio() {
        Station lejana = Station.builder()
                .id(2L).name("Estación Valparaíso").address("Muelle Barón")
                .latitude(-33.0458).longitude(-71.6197)
                .capacity(5).occupied(0).status(StationStatus.ACTIVE)
                .build();
        when(repository.findByStatusOrderByNameAsc(StationStatus.ACTIVE))
                .thenReturn(List.of(station, lejana));

        List<StationResponseDTO> cerca = service.getNearby(-33.4372, -70.6344, 5);

        assertThat(cerca).hasSize(1);
        assertThat(cerca.get(0).id()).isEqualTo(1L);
    }
}
