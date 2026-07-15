package cl.velourbe.notification.service;

import cl.velourbe.notification.model.dto.NotificationDTO;
import cl.velourbe.notification.model.entity.Notification;
import cl.velourbe.notification.model.enums.NotificationType;
import cl.velourbe.notification.repository.NotificationRepository;
import cl.velourbe.notification.exception.NotificationNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Notification Service
 * Handles business logic for notification operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    /**
     * Send a notification to a user
     * 
     * @param userId the ID of the user receiving the notification
     * @param type the notification type (EMAIL or SMS)
     * @param message the notification message content
     * @return the created notification DTO
     * @throws IllegalArgumentException if userId, type, or message is invalid
     */
    @Transactional
    public NotificationDTO sendNotification(Long userId, NotificationType type, String message) {
        log.debug("Sending {} notification to user {}", type, userId);
        
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId provided");
        }
        if (type == null) {
            throw new IllegalArgumentException("Notification type cannot be null");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        
        Notification notification = Notification.builder()
            .userId(userId)
            .type(type)
            .message(message)
            .sent(true)
            .build();
        
        Notification saved = notificationRepository.save(notification);
        log.info("Notification sent successfully to user {}", userId);
        
        return toDTO(saved);
    }
    
    /**
     * Retrieve notification history for a user
     * 
     * @param userId the ID of the user
     * @return list of notifications ordered by creation date (newest first)
     * @throws IllegalArgumentException if userId is invalid
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationHistory(Long userId) {
        log.debug("Retrieving notification history for user {}", userId);
        
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId provided");
        }
        
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        log.info("Retrieved {} notifications for user {}", notifications.size(), userId);
        
        return notifications.stream()
            .map(this::toDTO)
            .toList();
    }
    
    /**
     * Mark a notification as read
     * 
     * @param notificationId the ID of the notification to mark as read
     * @return the updated notification DTO
     * @throws NotificationNotFoundException if notification is not found
     */
    @Transactional
    public NotificationDTO markAsRead(Long notificationId) {
        log.debug("Marking notification {} as read", notificationId);
        
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotificationNotFoundException(
                "Notification not found with id: " + notificationId));
        
        notification.setSent(false);
        Notification updated = notificationRepository.save(notification);
        log.info("Notification {} marked as read", notificationId);
        
        return toDTO(updated);
    }
    
    private NotificationDTO toDTO(Notification notification) {
        return new NotificationDTO(
            notification.getId(),
            notification.getUserId(),
            notification.getType(),
            notification.getMessage(),
            notification.isSent(),
            notification.getCreatedAt()
        );
    }
}
