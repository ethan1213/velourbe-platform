package cl.velourbe.userauth.service;

import cl.velourbe.userauth.exception.*;
import cl.velourbe.userauth.model.dto.*;
import cl.velourbe.userauth.model.entity.User;
import cl.velourbe.userauth.repository.UserRepository;
import cl.velourbe.userauth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio de lógica de negocio para autenticación y registro de usuarios.
 * Coordina la validación de credenciales, el cifrado de contraseñas
 * y la generación de tokens JWT mediante {@link JwtUtil}.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Registra un nuevo usuario con rol CLIENT en el sistema.
     * Verifica que el email no esté ya registrado, hashea la contraseña
     * con BCrypt y genera un token JWT para uso inmediato.
     *
     * @param dto datos del nuevo usuario (email, password, fullName)
     * @return DTO con el token JWT generado y el rol "CLIENT"
     * @throws EmailAlreadyExistsException si el email ya está registrado
     */
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException(dto.getEmail());
        }
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setRole("CLIENT");
        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getEmail(), saved.getRole(), saved.getId());
        return new AuthResponseDTO(token, saved.getRole());
    }

    /**
     * Autentica un usuario existente verificando email y contraseña.
     * La verificación de contraseña compara contra el hash BCrypt almacenado.
     *
     * @param dto credenciales de acceso (email y password)
     * @return DTO con el token JWT generado y el rol del usuario
     * @throws InvalidCredentialsException si el email no existe o la contraseña no coincide
     */
    public AuthResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());
        return new AuthResponseDTO(token, user.getRole());
    }
}
