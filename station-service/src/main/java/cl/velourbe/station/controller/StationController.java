package cl.velourbe.station.controller;

import cl.velourbe.station.model.dto.CreateStationRequestDTO;
import cl.velourbe.station.model.dto.StationResponseDTO;
import cl.velourbe.station.service.StationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST controller for station endpoints.
 * All endpoints require authentication; admin-only operations use @PreAuthorize.
 */
@Slf4j
@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class StationController {

    private final StationService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<StationResponseDTO>> create(
            @Valid @RequestBody CreateStationRequestDTO dto) {
        log.info("POST /api/stations — name='{}'", dto.name());
        StationResponseDTO created = service.create(dto);
        URI location = linkTo(methodOn(StationController.class).getById(created.id())).toUri();
        return ResponseEntity.created(location).body(EntityModel.of(created,
                linkTo(methodOn(StationController.class).getById(created.id())).withSelfRel(),
                linkTo(methodOn(StationController.class).getAll()).withRel("stations")));
    }

    @GetMapping
    public CollectionModel<StationResponseDTO> getAll() {
        log.info("GET /api/stations");
        return CollectionModel.of(service.getAll(),
                linkTo(methodOn(StationController.class).getAll()).withSelfRel());
    }

    @GetMapping("/{id}")
    public EntityModel<StationResponseDTO> getById(@PathVariable Long id) {
        log.info("GET /api/stations/{}", id);
        return EntityModel.of(service.getById(id),
                linkTo(methodOn(StationController.class).getById(id)).withSelfRel());
    }

    @GetMapping("/nearby")
    public CollectionModel<StationResponseDTO> getNearby(@RequestParam double latitude,
                                                         @RequestParam double longitude,
                                                         @RequestParam(defaultValue = "2") double radiusKm) {
        log.info("GET /api/stations/nearby — lat={} lng={} radio={}km", latitude, longitude, radiusKm);
        return CollectionModel.of(service.getNearby(latitude, longitude, radiusKm),
                linkTo(methodOn(StationController.class).getNearby(latitude, longitude, radiusKm)).withSelfRel());
    }

    @PatchMapping("/{id}/dock")
    public EntityModel<StationResponseDTO> dock(@PathVariable Long id) {
        log.info("PATCH /api/stations/{}/dock", id);
        return EntityModel.of(service.dock(id),
                linkTo(methodOn(StationController.class).getById(id)).withSelfRel());
    }

    @PatchMapping("/{id}/undock")
    public EntityModel<StationResponseDTO> undock(@PathVariable Long id) {
        log.info("PATCH /api/stations/{}/undock", id);
        return EntityModel.of(service.undock(id),
                linkTo(methodOn(StationController.class).getById(id)).withSelfRel());
    }

    @PatchMapping("/{id}/maintenance")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<StationResponseDTO> maintenance(@PathVariable Long id) {
        log.info("PATCH /api/stations/{}/maintenance", id);
        return EntityModel.of(service.setMaintenance(id),
                linkTo(methodOn(StationController.class).getById(id)).withSelfRel());
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<StationResponseDTO> activate(@PathVariable Long id) {
        log.info("PATCH /api/stations/{}/activate", id);
        return EntityModel.of(service.activate(id),
                linkTo(methodOn(StationController.class).getById(id)).withSelfRel());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/stations/{}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
