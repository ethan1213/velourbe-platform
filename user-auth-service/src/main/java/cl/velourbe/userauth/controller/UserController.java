package cl.velourbe.userauth.controller;

import cl.velourbe.userauth.model.dto.UserResponseDTO;
import cl.velourbe.userauth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controlador REST para la administración de usuarios.
 * Todos los endpoints requieren rol ADMIN (configurado en {@code SecurityConfig}).
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users — Lista todos los usuarios registrados en el sistema.
     * Acceso restringido a ADMIN.
     *
     * @return 200 OK con la lista completa de usuarios
     */
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    /**
     * GET /api/users/active/{role} — Lista los usuarios activos filtrados por rol.
     * Usa la consulta JPQL personalizada del repositorio para mayor eficiencia.
     * Acceso restringido a ADMIN.
     *
     * @param role nombre del rol a filtrar ("CLIENT" o "ADMIN")
     * @return 200 OK con la lista de usuarios activos del rol indicado
     */
    @GetMapping("/active/{role}")
    public ResponseEntity<List<UserResponseDTO>> getActiveByRole(@PathVariable String role) {
        return ResponseEntity.ok(userService.findActiveByRole(role));
    }
}
