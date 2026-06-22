package cl.velourbe.userauth.controller;

import cl.velourbe.userauth.exception.GlobalExceptionHandler;
import cl.velourbe.userauth.model.dto.UserResponseDTO;
import cl.velourbe.userauth.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas a nivel de controller para {@link UserController}.
 * Usan MockMvc en modo standalone con {@link UserService} simulado por Mockito.
 * Los endpoints probados no dependen del objeto Authentication.
 */
class UserControllerTest {

    private MockMvc mockMvc;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = Mockito.mock(UserService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private UserResponseDTO buildDTO(Long id, String email, String role) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(id);
        dto.setEmail(email);
        dto.setFullName("Usuario Prueba");
        dto.setRole(role);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setActive(true);
        return dto;
    }

    @Test
    void getAll_retornaEstado200() throws Exception {
        when(userService.findAll()).thenReturn(List.of(
                buildDTO(1L, "admin@velourbe.cl", "ADMIN"),
                buildDTO(2L, "cliente@velourbe.cl", "CLIENT")));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());

        verify(userService).findAll();
    }

    @Test
    void getActiveByRole_retornaEstado200() throws Exception {
        when(userService.findActiveByRole("CLIENT")).thenReturn(List.of(
                buildDTO(2L, "cliente@velourbe.cl", "CLIENT")));

        mockMvc.perform(get("/api/users/active/CLIENT"))
                .andExpect(status().isOk());

        verify(userService).findActiveByRole("CLIENT");
    }
}
