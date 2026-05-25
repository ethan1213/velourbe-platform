package cl.velourbe.userauth.service;

import cl.velourbe.userauth.model.dto.UserResponseDTO;
import cl.velourbe.userauth.model.entity.User;
import cl.velourbe.userauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream().map(this::toDTO).toList();
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
