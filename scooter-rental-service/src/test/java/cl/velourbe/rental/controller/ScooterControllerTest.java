package cl.velourbe.rental.controller;

import cl.velourbe.rental.exception.GlobalExceptionHandler;
import cl.velourbe.rental.exception.ScooterNotFoundException;
import cl.velourbe.rental.model.dto.ScooterResponseDTO;
import cl.velourbe.rental.service.ScooterService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Pruebas a nivel de controller para {@link ScooterController}.
 * Usan MockMvc en modo standalone (sin contexto Spring ni base de datos),
 * con el servicio simulado mediante Mockito, para verificar el mapeo HTTP,
 * los códigos de estado y la serialización de las respuestas.
 */
class ScooterControllerTest {

    private MockMvc mockMvc;
    private ScooterService scooterService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        scooterService = Mockito.mock(ScooterService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ScooterController(scooterService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ScooterResponseDTO buildDTO(Long id, String serial) {
        ScooterResponseDTO dto = new ScooterResponseDTO();
        dto.setId(id);
        dto.setSerialCode(serial);
        dto.setModel("Xiaomi Pro 2");
        dto.setBattery(80);
        dto.setLocation("Plaza Italia");
        dto.setStatus("AVAILABLE");
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }

    @Test
    void getAll_retornaListaConEstado200() throws Exception {
        when(scooterService.findAll()).thenReturn(List.of(
                buildDTO(1L, "SC-001"), buildDTO(2L, "SC-002")));

        mockMvc.perform(get("/api/scooters"))
                .andExpect(status().isOk());

        verify(scooterService).findAll();
    }

    @Test
    void create_retornaEstado201ConPatineta() throws Exception {
        when(scooterService.create(any())).thenReturn(buildDTO(10L, "SC-010"));

        String body = """
                {"serialCode":"SC-010","model":"Xiaomi Pro 2","battery":80,"location":"Plaza Italia"}
                """;

        mockMvc.perform(post("/api/scooters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.serialCode").value("SC-010"));
    }

    @Test
    void getById_existente_retornaEstado200() throws Exception {
        when(scooterService.findById(3L)).thenReturn(buildDTO(3L, "SC-003"));

        mockMvc.perform(get("/api/scooters/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void getById_inexistente_retornaEstado404() throws Exception {
        when(scooterService.findById(99L)).thenThrow(new ScooterNotFoundException(99L));

        mockMvc.perform(get("/api/scooters/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_retornaEstado204() throws Exception {
        doNothing().when(scooterService).delete(5L);

        mockMvc.perform(delete("/api/scooters/5"))
                .andExpect(status().isNoContent());

        verify(scooterService).delete(5L);
    }
}
