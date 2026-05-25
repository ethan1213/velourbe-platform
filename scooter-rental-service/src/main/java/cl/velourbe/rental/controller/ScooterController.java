package cl.velourbe.rental.controller;

import cl.velourbe.rental.model.dto.*;
import cl.velourbe.rental.service.ScooterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/scooters")
@RequiredArgsConstructor
public class ScooterController {

    private final ScooterService scooterService;

    @GetMapping
    public ResponseEntity<List<ScooterResponseDTO>> getAll() {
        return ResponseEntity.ok(scooterService.findAll());
    }

    @GetMapping("/available")
    public ResponseEntity<List<ScooterResponseDTO>> getAvailable() {
        return ResponseEntity.ok(scooterService.findAvailable());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScooterResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(scooterService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ScooterResponseDTO> create(@Valid @RequestBody ScooterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scooterService.create(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scooterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
