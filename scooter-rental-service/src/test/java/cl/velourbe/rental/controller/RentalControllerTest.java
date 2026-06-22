package cl.velourbe.rental.controller;

import cl.velourbe.rental.exception.GlobalExceptionHandler;
import cl.velourbe.rental.exception.RentalNotFoundException;
import cl.velourbe.rental.model.dto.RentalResponseDTO;
import cl.velourbe.rental.service.RentalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas a nivel de controller para {@link RentalController}.
 * Usan MockMvc en modo standalone con el servicio simulado por Mockito.
 */
class RentalControllerTest {

    private MockMvc mockMvc;
    private RentalService rentalService;

    @BeforeEach
    void setUp() {
        rentalService = Mockito.mock(RentalService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new RentalController(rentalService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private RentalResponseDTO buildDTO(Long id, String status) {
        RentalResponseDTO dto = new RentalResponseDTO();
        dto.setId(id);
        dto.setUserId(3L);
        dto.setScooterId(1L);
        dto.setScooterModel("Xiaomi Pro 2");
        dto.setStartedAt(LocalDateTime.now());
        dto.setStatus(status);
        return dto;
    }

    @Test
    void start_retornaEstado201ConArriendo() throws Exception {
        when(rentalService.startRental(any())).thenReturn(buildDTO(1L, "ACTIVE"));

        mockMvc.perform(post("/api/rentals/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scooterId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void end_retornaEstado200ConArriendoCompletado() throws Exception {
        RentalResponseDTO completed = buildDTO(1L, "COMPLETED");
        completed.setTotalMinutes(30);
        when(rentalService.endRental(1L)).thenReturn(completed);

        mockMvc.perform(patch("/api/rentals/1/end"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.totalMinutes").value(30));
    }

    @Test
    void end_inexistente_retornaEstado404() throws Exception {
        when(rentalService.endRental(99L)).thenThrow(new RentalNotFoundException(99L));

        mockMvc.perform(patch("/api/rentals/99/end"))
                .andExpect(status().isNotFound());
    }

    @Test
    void myRentals_retornaEstado200() throws Exception {
        when(rentalService.myRentals()).thenReturn(List.of(buildDTO(1L, "ACTIVE")));

        mockMvc.perform(get("/api/rentals/my"))
                .andExpect(status().isOk());

        verify(rentalService).myRentals();
    }
}
