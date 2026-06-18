package cl.velourbe.userauth.controller;

import cl.velourbe.userauth.model.dto.*;
import cl.velourbe.userauth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<EntityModel<AuthResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO dto) {
        AuthResponseDTO response = authService.register(dto);
        EntityModel<AuthResponseDTO> model = EntityModel.of(response,
                linkTo(methodOn(AuthController.class).login(null)).withRel("login"));
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @PostMapping("/login")
    public ResponseEntity<EntityModel<AuthResponseDTO>> login(@Valid @RequestBody LoginRequestDTO dto) {
        AuthResponseDTO response = authService.login(dto);
        EntityModel<AuthResponseDTO> model = EntityModel.of(response,
                linkTo(methodOn(AuthController.class).login(null)).withSelfRel(),
                linkTo(methodOn(AuthController.class).register(null)).withRel("register"));
        return ResponseEntity.ok(model);
    }
}
