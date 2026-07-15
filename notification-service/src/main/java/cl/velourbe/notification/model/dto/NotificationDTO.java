package cl.velourbe.notification.model.dto;

import cl.velourbe.notification.model.enums.NotificationType;
import java.time.LocalDateTime;

/**
 * Notification Data Transfer Object
 */
public record NotificationDTO(
    Long id,
    Long userId,
    NotificationType type,
    String message,
    boolean sent,
    LocalDateTime createdAt
) {}
