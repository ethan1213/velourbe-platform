package cl.velourbe.userauth.service;

import cl.velourbe.userauth.model.dto.UserResponseDTO;
import cl.velourbe.userauth.model.entity.User;
import cl.velourbe.userauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Servicio de lógica de negocio para la gestión de usuarios.
 * Coordina el acceso a {@link UserRepository} y transforma entidades en DTOs.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Retorna la lista completa de todos los usuarios registrados en el sistema.
     *
     * @return lista de {@link UserResponseDTO} con todos los usuarios
     */
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream().map(this::toDTO).toList();
    }

    /**
     * Retorna los usuarios activos que tienen el rol indicado.
     * Usa la consulta JPQL personalizada {@code findActiveByRole} del repositorio.
     *
     * @param role nombre del rol a filtrar ("CLIENT" o "ADMIN")
     * @return lista de {@link UserResponseDTO} que coinciden con el rol y están activos
     */
    public List<UserResponseDTO> findActiveByRole(String role) {
        return userRepository.findActiveByRole(role).stream().map(this::toDTO).toList();
    }

    /**
     * Convierte una entidad {@link User} en su representación DTO de respuesta.
     *
     * @param u entidad de usuario
     * @return DTO con los datos públicos del usuario
     */
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
