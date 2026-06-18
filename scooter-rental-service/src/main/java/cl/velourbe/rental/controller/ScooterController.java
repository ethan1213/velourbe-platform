package cl.velourbe.rental.controller;

import cl.velourbe.rental.model.dto.*;
import cl.velourbe.rental.service.ScooterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@RestController
@RequestMapping("/api/scooters")
@RequiredArgsConstructor
public class ScooterController {

    private final ScooterService scooterService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollectionModel<EntityModel<ScooterResponseDTO>>> getAll() {
        List<EntityModel<ScooterResponseDTO>> models = scooterService.findAll().stream()
                .map(s -> toModel(s)).toList();
        return ResponseEntity.ok(CollectionModel.of(models,
                linkTo(methodOn(ScooterController.class).getAll()).withSelfRel()));
    }

    @GetMapping("/available")
    public ResponseEntity<CollectionModel<EntityModel<ScooterResponseDTO>>> getAvailable() {
        List<EntityModel<ScooterResponseDTO>> models = scooterService.findAvailable().stream()
                .map(s -> toModel(s)).toList();
        return ResponseEntity.ok(CollectionModel.of(models,
                linkTo(methodOn(ScooterController.class).getAvailable()).withSelfRel(),
                linkTo(methodOn(ScooterController.class).getAll()).withRel("scooters")));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<ScooterResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(scooterService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<ScooterResponseDTO>> create(@Valid @RequestBody ScooterRequestDTO dto) {
        EntityModel<ScooterResponseDTO> model = toModel(scooterService.create(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scooterService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/low-battery")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollectionModel<EntityModel<ScooterResponseDTO>>> getLowBattery(
            @RequestParam(defaultValue = "30") int threshold) {
        List<EntityModel<ScooterResponseDTO>> models = scooterService.findLowBattery(threshold).stream()
                .map(s -> toModel(s)).toList();
        return ResponseEntity.ok(CollectionModel.of(models,
                linkTo(methodOn(ScooterController.class).getLowBattery(threshold)).withSelfRel()));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollectionModel<EntityModel<ScooterResponseDTO>>> searchByLocation(
            @RequestParam String location) {
        List<EntityModel<ScooterResponseDTO>> models = scooterService.searchByLocation(location).stream()
                .map(s -> toModel(s)).toList();
        return ResponseEntity.ok(CollectionModel.of(models,
                linkTo(methodOn(ScooterController.class).searchByLocation(location)).withSelfRel()));
    }

    private EntityModel<ScooterResponseDTO> toModel(ScooterResponseDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(ScooterController.class).getById(dto.getId())).withSelfRel(),
                linkTo(methodOn(ScooterController.class).getAll()).withRel("scooters"));
    }
}
