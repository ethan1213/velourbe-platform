package cl.velourbe.userauth.controller;

import cl.velourbe.userauth.model.dto.*;
import cl.velourbe.userauth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación de usuarios.
 * Expone los endpoints públicos de registro y login del sistema.
 * No requiere token JWT para acceder a estos endpoints.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register — Registra un nuevo usuario con rol CLIENT.
     * Valida los campos del body y retorna un token JWT listo para usar.
     *
     * @param dto datos de registro (email, password, fullName)
     * @return 201 Created con token JWT y rol asignado
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(dto));
    }

    /**
     * POST /api/auth/login — Autentica un usuario existente y emite un token JWT.
     *
     * @param dto credenciales de acceso (email y password)
     * @return 200 OK con token JWT y rol del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}
