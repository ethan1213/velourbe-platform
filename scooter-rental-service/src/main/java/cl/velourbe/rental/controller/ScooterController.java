package cl.velourbe.rental.controller;

import cl.velourbe.rental.model.dto.*;
import cl.velourbe.rental.service.ScooterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controlador REST para la gestión del inventario de patinetas.
 * Todos los endpoints requieren rol ADMIN (configurado en {@code SecurityConfig}).
 */
@RestController
@RequestMapping("/api/scooters")
@RequiredArgsConstructor
public class ScooterController {

    private final ScooterService scooterService;

    /**
     * GET /api/scooters — Lista todas las patinetas del sistema sin importar su estado.
     *
     * @return 200 OK con la lista completa de patinetas
     */
    @GetMapping
    public ResponseEntity<List<ScooterResponseDTO>> getAll() {
        return ResponseEntity.ok(scooterService.findAll());
    }

    /**
     * GET /api/scooters/available — Lista solo las patinetas con estado AVAILABLE.
     *
     * @return 200 OK con patinetas disponibles para arrendar
     */
    @GetMapping("/available")
    public ResponseEntity<List<ScooterResponseDTO>> getAvailable() {
        return ResponseEntity.ok(scooterService.findAvailable());
    }

    /**
     * GET /api/scooters/{id} — Retorna el detalle de una patineta específica.
     *
     * @param id identificador de la patineta
     * @return 200 OK con la patineta, o 404 si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<ScooterResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(scooterService.findById(id));
    }

    /**
     * POST /api/scooters — Registra una nueva patineta en el sistema.
     * La patineta se crea con estado AVAILABLE por defecto.
     *
     * @param dto datos de la nueva patineta (serialCode, model, battery, location)
     * @return 201 Created con la patineta registrada e ID asignado
     */
    @PostMapping
    public ResponseEntity<ScooterResponseDTO> create(@Valid @RequestBody ScooterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scooterService.create(dto));
    }

    /**
     * DELETE /api/scooters/{id} — Elimina una patineta del sistema de forma permanente.
     *
     * @param id identificador de la patineta a eliminar
     * @return 204 No Content si se eliminó, o 404 si no existe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scooterService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/scooters/low-battery?threshold=30 — Retorna patinetas con batería
     * inferior al umbral indicado, ordenadas de menor a mayor batería.
     * Usa la consulta JPQL personalizada del repositorio.
     *
     * @param threshold porcentaje máximo de batería (por defecto 30)
     * @return 200 OK con patinetas que necesitan recarga
     */
    @GetMapping("/low-battery")
    public ResponseEntity<List<ScooterResponseDTO>> getLowBattery(
            @RequestParam(defaultValue = "30") int threshold) {
        return ResponseEntity.ok(scooterService.findLowBattery(threshold));
    }

    /**
     * GET /api/scooters/search?location=plaza — Busca patinetas por ubicación parcial.
     * La búsqueda es case-insensitive. Usa la consulta JPQL personalizada del repositorio.
     *
     * @param location texto parcial de la ubicación a buscar
     * @return 200 OK con patinetas cuya ubicación coincide
     */
    @GetMapping("/search")
    public ResponseEntity<List<ScooterResponseDTO>> searchByLocation(
            @RequestParam String location) {
        return ResponseEntity.ok(scooterService.searchByLocation(location));
    }
}
