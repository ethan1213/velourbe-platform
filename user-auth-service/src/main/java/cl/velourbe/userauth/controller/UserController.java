package cl.velourbe.userauth.controller;

import cl.velourbe.userauth.model.dto.UserResponseDTO;
import cl.velourbe.userauth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollectionModel<EntityModel<UserResponseDTO>>> getAll() {
        log.debug("GET /api/users — listando todos los usuarios");
        List<EntityModel<UserResponseDTO>> models = userService.findAll().stream()
                .map(u -> EntityModel.of(u,
                        linkTo(methodOn(UserController.class).getById(u.getId(), null)).withSelfRel()))
                .toList();
        return ResponseEntity.ok(CollectionModel.of(models,
                linkTo(methodOn(UserController.class).getAll()).withSelfRel()));
    }

    /**
     * Permite a un ADMIN obtener cualquier perfil, o a un usuario autenticado obtener el suyo propio.
     * El BFF usa este endpoint con el token del usuario para obtener su perfil.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponseDTO>> getById(
            @PathVariable Long id,
            Authentication authentication) {
        log.debug("GET /api/users/{}", id);
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Long currentUserId = null;
        if (authentication instanceof UsernamePasswordAuthenticationToken token
                && token.getDetails() instanceof Long uid) {
            currentUserId = uid;
        }
        if (!isAdmin && !id.equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return userService.findById(id)
                .map(u -> EntityModel.of(u,
                        linkTo(methodOn(UserController.class).getById(id, null)).withSelfRel(),
                        linkTo(methodOn(UserController.class).getAll()).withRel("users")))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollectionModel<EntityModel<UserResponseDTO>>> getActiveByRole(@PathVariable String role) {
        log.debug("GET /api/users/active/{}", role);
        List<EntityModel<UserResponseDTO>> models = userService.findActiveByRole(role).stream()
                .map(u -> EntityModel.of(u,
                        linkTo(methodOn(UserController.class).getById(u.getId(), null)).withSelfRel()))
                .toList();
        return ResponseEntity.ok(CollectionModel.of(models,
                linkTo(methodOn(UserController.class).getActiveByRole(role)).withSelfRel(),
                linkTo(methodOn(UserController.class).getAll()).withRel("users")));
    }
}
