package cl.velourbe.rental.controller;

import cl.velourbe.rental.model.dto.*;
import cl.velourbe.rental.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @PostMapping("/start")
    public ResponseEntity<RentalResponseDTO> start(@Valid @RequestBody RentalRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rentalService.startRental(dto));
    }

    @PatchMapping("/{id}/end")
    public ResponseEntity<RentalResponseDTO> end(@PathVariable Long id) {
        return ResponseEntity.ok(rentalService.endRental(id));
    }

    @GetMapping("/my")
    public ResponseEntity<List<RentalResponseDTO>> myRentals() {
        return ResponseEntity.ok(rentalService.myRentals());
    }
}
