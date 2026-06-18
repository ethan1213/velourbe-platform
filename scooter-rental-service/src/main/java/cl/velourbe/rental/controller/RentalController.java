package cl.velourbe.rental.controller;

import cl.velourbe.rental.model.dto.*;
import cl.velourbe.rental.service.RentalService;
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
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @PostMapping("/start")
    public ResponseEntity<EntityModel<RentalResponseDTO>> start(@Valid @RequestBody RentalRequestDTO dto) {
        RentalResponseDTO response = rentalService.startRental(dto);
        EntityModel<RentalResponseDTO> model = EntityModel.of(response,
                linkTo(methodOn(RentalController.class).end(response.getId())).withRel("end"),
                linkTo(methodOn(RentalController.class).myRentals()).withRel("my-rentals"));
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @PatchMapping("/{id}/end")
    public ResponseEntity<EntityModel<RentalResponseDTO>> end(@PathVariable Long id) {
        RentalResponseDTO response = rentalService.endRental(id);
        EntityModel<RentalResponseDTO> model = EntityModel.of(response,
                linkTo(methodOn(RentalController.class).myRentals()).withRel("my-rentals"));
        return ResponseEntity.ok(model);
    }

    @GetMapping("/my")
    public ResponseEntity<CollectionModel<EntityModel<RentalResponseDTO>>> myRentals() {
        List<EntityModel<RentalResponseDTO>> models = rentalService.myRentals().stream()
                .map(r -> EntityModel.of(r,
                        linkTo(methodOn(RentalController.class).myRentals()).withSelfRel()))
                .toList();
        return ResponseEntity.ok(CollectionModel.of(models,
                linkTo(methodOn(RentalController.class).myRentals()).withSelfRel()));
    }

    @GetMapping("/long")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollectionModel<EntityModel<RentalResponseDTO>>> longRentals(
            @RequestParam(defaultValue = "30") int minMinutes) {
        List<EntityModel<RentalResponseDTO>> models = rentalService.findLongRentals(minMinutes).stream()
                .map(r -> EntityModel.of(r,
                        linkTo(methodOn(RentalController.class).myRentals()).withSelfRel()))
                .toList();
        return ResponseEntity.ok(CollectionModel.of(models,
                linkTo(methodOn(RentalController.class).longRentals(minMinutes)).withSelfRel()));
    }
}
