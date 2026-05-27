package cl.velourbe.rental.controller;

import cl.velourbe.rental.model.dto.*;
import cl.velourbe.rental.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controlador REST para el ciclo de vida de los arriendos.
 * Todos los endpoints requieren autenticación con token JWT válido.
 */
@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    /**
     * POST /api/rentals/start — Inicia un nuevo arriendo para el usuario autenticado.
     * Cambia el estado de la patineta a IN_USE de forma transaccional.
     *
     * @param dto contiene el ID de la patineta a arrendar
     * @return 201 Created con el arriendo creado y estado ACTIVE
     */
    @PostMapping("/start")
    public ResponseEntity<RentalResponseDTO> start(@Valid @RequestBody RentalRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rentalService.startRental(dto));
    }

    /**
     * PATCH /api/rentals/{id}/end — Finaliza un arriendo activo y calcula la duración.
     * Devuelve la patineta al estado AVAILABLE de forma transaccional.
     *
     * @param id identificador del arriendo a finalizar
     * @return 200 OK con el arriendo actualizado, duración en minutos y estado COMPLETED
     */
    @PatchMapping("/{id}/end")
    public ResponseEntity<RentalResponseDTO> end(@PathVariable Long id) {
        return ResponseEntity.ok(rentalService.endRental(id));
    }

    /**
     * GET /api/rentals/my — Retorna el historial completo de arriendos del usuario autenticado.
     *
     * @return 200 OK con todos los arriendos del usuario actual
     */
    @GetMapping("/my")
    public ResponseEntity<List<RentalResponseDTO>> myRentals() {
        return ResponseEntity.ok(rentalService.myRentals());
    }

    /**
     * GET /api/rentals/long?minMinutes=30 — Retorna arriendos completados con
     * duración igual o mayor al mínimo indicado.
     * Usa la consulta JPQL personalizada del repositorio. Requiere rol ADMIN.
     *
     * @param minMinutes duración mínima en minutos (por defecto 30)
     * @return 200 OK con arriendos largos completados
     */
    @GetMapping("/long")
    public ResponseEntity<List<RentalResponseDTO>> longRentals(
            @RequestParam(defaultValue = "30") int minMinutes) {
        return ResponseEntity.ok(rentalService.findLongRentals(minMinutes));
    }
}
