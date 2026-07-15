package cl.velourbe.logistics.controller;

import cl.velourbe.logistics.model.dto.LocationDTO;
import cl.velourbe.logistics.service.LogisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping("/api/logistics")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class LogisticsController {

    private final LogisticsService logisticsService;

    @PostMapping("/locations")
    public ResponseEntity<EntityModel<LocationDTO>> recordLocation(
            @RequestParam Long scooterId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        log.info("POST /api/logistics/locations scooterId={}", scooterId);
        LocationDTO location = logisticsService.recordScooterLocation(scooterId, latitude, longitude);
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(location));
    }

    @GetMapping("/locations/{scooterId}")
    public EntityModel<LocationDTO> getLocation(@PathVariable Long scooterId) {
        log.info("GET /api/logistics/locations/{}", scooterId);
        return toModel(logisticsService.getScooterLocation(scooterId));
    }

    @GetMapping("/nearby")
    public CollectionModel<EntityModel<LocationDTO>> nearby(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "1.0") Double radiusKm) {
        log.info("GET /api/logistics/nearby lat={} lon={} radius={}", latitude, longitude, radiusKm);
        List<EntityModel<LocationDTO>> models = logisticsService
                .getScootersInArea(latitude, longitude, radiusKm)
                .stream()
                .map(this::toModel)
                .toList();
        return CollectionModel.of(models,
                linkTo(methodOn(LogisticsController.class).nearby(latitude, longitude, radiusKm)).withSelfRel());
    }

    private EntityModel<LocationDTO> toModel(LocationDTO location) {
        return EntityModel.of(location,
                linkTo(methodOn(LogisticsController.class).getLocation(location.scooterId())).withSelfRel());
    }
}
