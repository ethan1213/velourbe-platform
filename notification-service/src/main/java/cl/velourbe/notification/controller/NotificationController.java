package cl.velourbe.notification.controller;

import cl.velourbe.notification.model.dto.NotificationDTO;
import cl.velourbe.notification.model.enums.NotificationType;
import cl.velourbe.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<EntityModel<NotificationDTO>> send(
            @RequestParam NotificationType type,
            @RequestParam String message) {
        Long userId = currentUserId();
        log.info("POST /api/notifications userId={} type={}", userId, type);
        NotificationDTO created = notificationService.sendNotification(userId, type, message);
        return ResponseEntity.status(HttpStatus.CREATED).body(EntityModel.of(created,
                linkTo(methodOn(NotificationController.class).history()).withRel("history"),
                linkTo(methodOn(NotificationController.class).markAsRead(created.id())).withRel("mark-read")));
    }

    @GetMapping("/history")
    public CollectionModel<EntityModel<NotificationDTO>> history() {
        Long userId = currentUserId();
        log.info("GET /api/notifications/history userId={}", userId);
        List<EntityModel<NotificationDTO>> models = notificationService.getNotificationHistory(userId)
                .stream()
                .map(n -> EntityModel.of(n,
                        linkTo(methodOn(NotificationController.class).markAsRead(n.id())).withRel("mark-read")))
                .toList();
        return CollectionModel.of(models,
                linkTo(methodOn(NotificationController.class).history()).withSelfRel());
    }

    @PatchMapping("/{id}/read")
    public EntityModel<NotificationDTO> markAsRead(@PathVariable Long id) {
        log.info("PATCH /api/notifications/{}/read", id);
        NotificationDTO updated = notificationService.markAsRead(id);
        return EntityModel.of(updated,
                linkTo(methodOn(NotificationController.class).history()).withRel("history"));
    }

    private Long currentUserId() {
        return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
