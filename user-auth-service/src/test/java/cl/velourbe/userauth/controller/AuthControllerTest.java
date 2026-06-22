package cl.velourbe.userauth.controller;

import cl.velourbe.userauth.exception.GlobalExceptionHandler;
import cl.velourbe.userauth.exception.InvalidCredentialsException;
import cl.velourbe.userauth.model.dto.AuthResponseDTO;
import cl.velourbe.userauth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas a nivel de controller para {@link AuthController}.
 * Usan MockMvc en modo standalone con {@link AuthService} simulado por Mockito.
 */
class AuthControllerTest {

    private MockMvc mockMvc;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = Mockito.mock(AuthService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void register_retornaEstado201ConToken() throws Exception {
        when(authService.register(any())).thenReturn(new AuthResponseDTO("jwt-token", "CLIENT"));

        String body = """
                {"email":"nuevo@velourbe.cl","password":"cliente123","fullName":"Nuevo Usuario"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.role").value("CLIENT"));
    }

    @Test
    void login_retornaEstado200ConToken() throws Exception {
        when(authService.login(any())).thenReturn(new AuthResponseDTO("jwt-token", "ADMIN"));

        String body = """
                {"email":"admin@velourbe.cl","password":"admin123"}
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void login_credencialesInvalidas_retornaEstado401() throws Exception {
        when(authService.login(any())).thenThrow(new InvalidCredentialsException());

        String body = """
                {"email":"admin@velourbe.cl","password":"incorrecta"}
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}
