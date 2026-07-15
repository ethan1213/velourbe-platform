package cl.velourbe.maintenance.controller;

import cl.velourbe.maintenance.model.dto.MaintenanceIssueDTO;
import cl.velourbe.maintenance.model.enums.IssueStatus;
import cl.velourbe.maintenance.service.MaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * REST controller for maintenance issue endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/maintenance/issues")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MaintenanceController {

    private final MaintenanceService service;

    @PostMapping
    public ResponseEntity<EntityModel<MaintenanceIssueDTO>> create(@Valid @RequestBody MaintenanceIssueDTO dto) {
        log.info("POST /api/maintenance/issues scooterId={}", dto.scooterId());
        MaintenanceIssueDTO created = service.createIssue(dto);
        URI location = linkTo(methodOn(MaintenanceController.class).getById(created.id())).toUri();
        return ResponseEntity.created(location).body(toModel(created));
    }

    @GetMapping("/{id}")
    public EntityModel<MaintenanceIssueDTO> getById(@PathVariable Long id) {
        log.info("GET /api/maintenance/issues/{}", id);
        return toModel(service.getIssueById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CollectionModel<EntityModel<MaintenanceIssueDTO>> getAll(
            @RequestParam(required = false) IssueStatus status) {
        log.info("GET /api/maintenance/issues status={}", status);
        var issues = (status == null ? service.getAllIssues() : service.getIssuesByStatus(status))
                .stream()
                .map(this::toModel)
                .toList();
        return CollectionModel.of(issues,
                linkTo(methodOn(MaintenanceController.class).getAll(status)).withSelfRel());
    }

    @GetMapping("/scooter/{scooterId}")
    public CollectionModel<EntityModel<MaintenanceIssueDTO>> getByScooter(@PathVariable Long scooterId) {
        log.info("GET /api/maintenance/issues/scooter/{}", scooterId);
        var issues = service.getIssuesByScooter(scooterId).stream()
                .map(this::toModel)
                .toList();
        return CollectionModel.of(issues,
                linkTo(methodOn(MaintenanceController.class).getByScooter(scooterId)).withSelfRel());
    }

    @PatchMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<MaintenanceIssueDTO> markInReview(@PathVariable Long id) {
        log.info("PATCH /api/maintenance/issues/{}/review", id);
        return toModel(service.markInReview(id));
    }

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<MaintenanceIssueDTO> startWork(@PathVariable Long id) {
        log.info("PATCH /api/maintenance/issues/{}/start", id);
        return toModel(service.startWork(id));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<MaintenanceIssueDTO> resolve(@PathVariable Long id,
                                                    @RequestBody MaintenanceIssueDTO dto) {
        log.info("PATCH /api/maintenance/issues/{}/resolve", id);
        return toModel(service.resolveIssue(id, dto.resolutionNotes()));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<MaintenanceIssueDTO> close(@PathVariable Long id) {
        log.info("PATCH /api/maintenance/issues/{}/close", id);
        return toModel(service.closeIssue(id));
    }

    private EntityModel<MaintenanceIssueDTO> toModel(MaintenanceIssueDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(MaintenanceController.class).getById(dto.id())).withSelfRel(),
                linkTo(methodOn(MaintenanceController.class).getByScooter(dto.scooterId())).withRel("scooter-issues"));
    }
}
