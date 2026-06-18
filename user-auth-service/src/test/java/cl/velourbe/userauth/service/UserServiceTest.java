package cl.velourbe.userauth.service;

import cl.velourbe.userauth.model.dto.UserResponseDTO;
import cl.velourbe.userauth.model.entity.User;
import cl.velourbe.userauth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks UserService userService;

    private User buildUser(Long id, String email, String role) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setFullName("Usuario " + id);
        u.setRole(role);
        u.setCreatedAt(LocalDateTime.now());
        u.setActive(true);
        return u;
    }

    @Test
    void findAll_retornaListaCompleta() {
        when(userRepository.findAll()).thenReturn(List.of(
                buildUser(1L, "admin@velourbe.cl", "ADMIN"),
                buildUser(2L, "client@velourbe.cl", "CLIENT")
        ));

        List<UserResponseDTO> result = userService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEmail()).isEqualTo("admin@velourbe.cl");
    }

    @Test
    void findById_existente_retornaDTO() {
        User u = buildUser(5L, "user5@velourbe.cl", "CLIENT");
        when(userRepository.findById(5L)).thenReturn(Optional.of(u));

        Optional<UserResponseDTO> result = userService.findById(5L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(5L);
        assertThat(result.get().getEmail()).isEqualTo("user5@velourbe.cl");
    }

    @Test
    void findById_inexistente_retornaVacio() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<UserResponseDTO> result = userService.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void findActiveByRole_filtraPorRol() {
        when(userRepository.findActiveByRole("CLIENT")).thenReturn(List.of(
                buildUser(2L, "client@velourbe.cl", "CLIENT")
        ));

        List<UserResponseDTO> result = userService.findActiveByRole("CLIENT");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo("CLIENT");
    }
}
