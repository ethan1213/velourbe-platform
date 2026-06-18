package cl.velourbe.userauth.service;

import cl.velourbe.userauth.model.dto.UserResponseDTO;
import cl.velourbe.userauth.model.entity.User;
import cl.velourbe.userauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponseDTO> findAll() {
        log.debug("Listando todos los usuarios");
        return userRepository.findAll().stream().map(this::toDTO).toList();
    }

    public List<UserResponseDTO> findActiveByRole(String role) {
        log.debug("Listando usuarios activos con rol={}", role);
        return userRepository.findActiveByRole(role).stream().map(this::toDTO).toList();
    }

    public Optional<UserResponseDTO> findById(Long id) {
        log.debug("Buscando usuario id={}", id);
        return userRepository.findById(id).map(this::toDTO);
    }

    private UserResponseDTO toDTO(User u) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(u.getId());
        dto.setEmail(u.getEmail());
        dto.setFullName(u.getFullName());
        dto.setRole(u.getRole());
        dto.setCreatedAt(u.getCreatedAt());
        dto.setActive(u.getActive());
        return dto;
    }
}
