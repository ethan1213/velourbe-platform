package cl.velourbe.notification.repository;

import cl.velourbe.notification.model.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Notification JPA Repository
 * Provides data access operations for notifications
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Find all notifications for a specific user
     * 
     * @param userId the user ID
     * @return list of notifications ordered by creation date descending
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
}
