package cl.velourbe.support.controller;

import cl.velourbe.support.model.dto.CreateTicketRequestDTO;
import cl.velourbe.support.model.dto.SupportTicketResponseDTO;
import cl.velourbe.support.model.dto.UpdateTicketRequestDTO;
import cl.velourbe.support.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST controller for support ticket endpoints.
 * All endpoints require authentication; admin-only operations use @PreAuthorize.
 */
@Slf4j
@RestController
@RequestMapping("/api/support/tickets")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SupportTicketController {

    private final SupportTicketService service;

    @PostMapping
    public ResponseEntity<EntityModel<SupportTicketResponseDTO>> create(
            @Valid @RequestBody CreateTicketRequestDTO dto) {
        Long userId = currentUserId();
        log.info("POST /api/support/tickets — userId={}", userId);
        SupportTicketResponseDTO created = service.create(userId, dto);
        URI location = linkTo(methodOn(SupportTicketController.class).getById(created.id())).toUri();
        return ResponseEntity.created(location).body(EntityModel.of(created,
                linkTo(methodOn(SupportTicketController.class).getById(created.id())).withSelfRel(),
                linkTo(methodOn(SupportTicketController.class).getMyTickets()).withRel("my-tickets")));
    }

    @GetMapping("/my")
    public CollectionModel<SupportTicketResponseDTO> getMyTickets() {
        Long userId = currentUserId();
        log.info("GET /api/support/tickets/my — userId={}", userId);
        List<SupportTicketResponseDTO> list = service.getMyTickets(userId);
        return CollectionModel.of(list,
                linkTo(methodOn(SupportTicketController.class).getMyTickets()).withSelfRel());
    }

    @GetMapping("/{id}")
    public EntityModel<SupportTicketResponseDTO> getById(@PathVariable Long id) {
        log.info("GET /api/support/tickets/{}", id);
        SupportTicketResponseDTO t = service.getById(id);
        return EntityModel.of(t,
                linkTo(methodOn(SupportTicketController.class).getById(id)).withSelfRel());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CollectionModel<SupportTicketResponseDTO> getAll() {
        log.info("GET /api/support/tickets — admin listing all");
        return CollectionModel.of(service.getAll(),
                linkTo(methodOn(SupportTicketController.class).getAll()).withSelfRel());
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<SupportTicketResponseDTO> assign(@PathVariable Long id,
                                                        @RequestBody UpdateTicketRequestDTO dto) {
        log.info("PATCH /api/support/tickets/{}/assign — assignedTo={}", id, dto.assignedTo());
        return EntityModel.of(service.assign(id, dto.assignedTo()),
                linkTo(methodOn(SupportTicketController.class).getById(id)).withSelfRel());
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<SupportTicketResponseDTO> resolve(@PathVariable Long id,
                                                         @RequestBody UpdateTicketRequestDTO dto) {
        log.info("PATCH /api/support/tickets/{}/resolve", id);
        return EntityModel.of(service.resolve(id, dto.resolutionNotes()),
                linkTo(methodOn(SupportTicketController.class).getById(id)).withSelfRel());
    }

    @PatchMapping("/{id}/close")
    public EntityModel<SupportTicketResponseDTO> close(@PathVariable Long id) {
        log.info("PATCH /api/support/tickets/{}/close", id);
        return EntityModel.of(service.close(id),
                linkTo(methodOn(SupportTicketController.class).getById(id)).withSelfRel());
    }

    private Long currentUserId() {
        return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
