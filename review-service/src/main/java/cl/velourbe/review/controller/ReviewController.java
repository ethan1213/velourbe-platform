package cl.velourbe.review.controller;

import cl.velourbe.review.model.dto.CreateReviewRequestDTO;
import cl.velourbe.review.model.dto.ReviewResponseDTO;
import cl.velourbe.review.model.dto.ScooterRatingDTO;
import cl.velourbe.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
 * REST controller for review endpoints.
 * All endpoints require authentication; admin-only operations use @PreAuthorize.
 */
@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ReviewController {

    private final ReviewService service;

    @PostMapping
    public ResponseEntity<EntityModel<ReviewResponseDTO>> create(
            @Valid @RequestBody CreateReviewRequestDTO dto) {
        Long userId = currentUserId();
        log.info("POST /api/reviews — userId={}", userId);
        ReviewResponseDTO created = service.create(userId, dto);
        URI location = linkTo(methodOn(ReviewController.class).getById(created.id())).toUri();
        return ResponseEntity.created(location).body(EntityModel.of(created,
                linkTo(methodOn(ReviewController.class).getById(created.id())).withSelfRel(),
                linkTo(methodOn(ReviewController.class).getMyReviews()).withRel("my-reviews")));
    }

    @GetMapping("/my")
    public CollectionModel<ReviewResponseDTO> getMyReviews() {
        Long userId = currentUserId();
        log.info("GET /api/reviews/my — userId={}", userId);
        List<ReviewResponseDTO> list = service.getMyReviews(userId);
        return CollectionModel.of(list,
                linkTo(methodOn(ReviewController.class).getMyReviews()).withSelfRel());
    }

    @GetMapping("/{id}")
    public EntityModel<ReviewResponseDTO> getById(@PathVariable Long id) {
        log.info("GET /api/reviews/{}", id);
        return EntityModel.of(service.getById(id),
                linkTo(methodOn(ReviewController.class).getById(id)).withSelfRel());
    }

    @GetMapping("/scooter/{scooterId}")
    public CollectionModel<ReviewResponseDTO> getByScooter(@PathVariable Long scooterId) {
        log.info("GET /api/reviews/scooter/{}", scooterId);
        return CollectionModel.of(service.getByScooter(scooterId),
                linkTo(methodOn(ReviewController.class).getByScooter(scooterId)).withSelfRel());
    }

    @GetMapping("/scooter/{scooterId}/rating")
    public EntityModel<ScooterRatingDTO> getScooterRating(@PathVariable Long scooterId) {
        log.info("GET /api/reviews/scooter/{}/rating", scooterId);
        return EntityModel.of(service.getScooterRating(scooterId),
                linkTo(methodOn(ReviewController.class).getScooterRating(scooterId)).withSelfRel());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CollectionModel<ReviewResponseDTO> getAll() {
        log.info("GET /api/reviews — admin listing all");
        return CollectionModel.of(service.getAll(),
                linkTo(methodOn(ReviewController.class).getAll()).withSelfRel());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/reviews/{}", id);
        service.delete(id, currentUserId(), isAdmin());
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId() {
        return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
