package cl.velourbe.userauth.service;

import cl.velourbe.userauth.exception.EmailAlreadyExistsException;
import cl.velourbe.userauth.exception.InvalidCredentialsException;
import cl.velourbe.userauth.model.dto.AuthResponseDTO;
import cl.velourbe.userauth.model.dto.LoginRequestDTO;
import cl.velourbe.userauth.model.dto.RegisterRequestDTO;
import cl.velourbe.userauth.model.entity.User;
import cl.velourbe.userauth.repository.UserRepository;
import cl.velourbe.userauth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @InjectMocks AuthService authService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("test@velourbe.cl");
        existingUser.setPasswordHash("$2a$10$hashed");
        existingUser.setFullName("Test User");
        existingUser.setRole("CLIENT");
    }

    @Test
    void register_exitoso_retornaToken() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("nuevo@velourbe.cl");
        dto.setPassword("pass123");
        dto.setFullName("Nuevo Usuario");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(jwtUtil.generateToken(anyString(), anyString(), anyLong())).thenReturn("jwt-token");

        AuthResponseDTO result = authService.register(dto);

        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getRole()).isEqualTo("CLIENT");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_emailDuplicado_lanzaExcepcion() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("existente@velourbe.cl");
        dto.setPassword("pass");
        dto.setFullName("Ya existe");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_credencialesCorrectas_retornaToken() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("test@velourbe.cl");
        dto.setPassword("pass123");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(dto.getPassword(), existingUser.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString(), anyLong())).thenReturn("jwt-token");

        AuthResponseDTO result = authService.login(dto);

        assertThat(result.getToken()).isEqualTo("jwt-token");
    }

    @Test
    void login_emailInexistente_lanzaExcepcion() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("noexiste@velourbe.cl");
        dto.setPassword("pass");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_passwordIncorrecta_lanzaExcepcion() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("test@velourbe.cl");
        dto.setPassword("wrongpass");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(dto.getPassword(), existingUser.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
