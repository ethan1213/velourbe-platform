package cl.velourbe.analytics.repository;

import cl.velourbe.analytics.model.entity.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for AnalyticsEvent entities.
 * Provides CRUD operations and custom queries for analytics data.
 */
@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {
    
    /**
     * Finds all events for a specific user.
     * 
     * @param userId the user ID
     * @return list of analytics events for the user
     */
    List<AnalyticsEvent> findByUserId(Long userId);
    
    /**
     * Finds all events for a specific rental.
     * 
     * @param rentalId the rental ID
     * @return list of analytics events for the rental
     */
    List<AnalyticsEvent> findByRentalId(Long rentalId);
    
    /**
     * Calculates total revenue across all events.
     * 
     * @return sum of all amounts
     */
    @Query("SELECT COALESCE(SUM(ae.amount), 0) FROM AnalyticsEvent ae")
    BigDecimal getTotalRevenue();
    
    /**
     * Counts total number of rental events.
     * 
     * @return count of all events
     */
    @Query("SELECT COUNT(ae) FROM AnalyticsEvent ae")
    Long getTotalEvents();
    
    /**
     * Calculates total revenue for a specific user.
     * 
     * @param userId the user ID
     * @return sum of amounts for the user
     */
    @Query("SELECT COALESCE(SUM(ae.amount), 0) FROM AnalyticsEvent ae WHERE ae.userId = :userId")
    BigDecimal getTotalRevenueByUserId(@Param("userId") Long userId);
}
