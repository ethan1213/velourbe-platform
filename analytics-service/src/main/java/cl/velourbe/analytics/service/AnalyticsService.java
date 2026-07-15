package cl.velourbe.analytics.service;

import cl.velourbe.analytics.model.dto.AnalyticsDTO;
import cl.velourbe.analytics.model.entity.AnalyticsEvent;
import cl.velourbe.analytics.repository.AnalyticsEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service layer for analytics operations.
 * Handles all business logic for recording rental events and generating statistics.
 * All public methods include comprehensive Javadoc as per architecture requirements.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {
    
    private final AnalyticsEventRepository analyticsEventRepository;
    
    /**
     * Records a rental event in the analytics system.
     * 
     * Creates a new analytics event with the provided rental information including
     * rental ID, user ID, and transaction amount. Event is persisted to database
     * for later statistical analysis.
     * 
     * @param rentalId the rental ID associated with the event
     * @param userId the user ID who performed the rental
     * @param amount the transaction amount in CLP currency
     * @return the recorded analytics event DTO with generated ID and timestamp
     */
    @Transactional
    public AnalyticsDTO recordRentalEvent(Long rentalId, Long userId, BigDecimal amount) {
        log.info("Recording rental event: rentalId={}, userId={}, amount={}", rentalId, userId, amount);
        
        AnalyticsEvent event = AnalyticsEvent.builder()
            .rentalId(rentalId)
            .userId(userId)
            .amount(amount)
            .eventType("RENTAL")
            .description("Rental event recorded")
            .build();
        
        event = analyticsEventRepository.save(event);
        log.info("Rental event recorded with id={}", event.getId());
        
        return toDTO(event);
    }
    
    /**
     * Retrieves statistical data for a specific user.
     * 
     * Aggregates all analytics events for the given user including total number
     * of rentals, total revenue generated, and event history. Returns comprehensive
     * user statistics for analytics and reporting.
     * 
     * @param userId the user ID to retrieve statistics for
     * @return map containing user statistics including event count and total revenue
     */
    public Map<String, Object> getUserStats(Long userId) {
        log.debug("Retrieving statistics for userId={}", userId);
        
        List<AnalyticsEvent> userEvents = analyticsEventRepository.findByUserId(userId);
        BigDecimal totalRevenue = analyticsEventRepository.getTotalRevenueByUserId(userId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", userId);
        stats.put("totalEvents", userEvents.size());
        stats.put("totalRevenue", totalRevenue);
        stats.put("events", userEvents.stream().map(this::toDTO).toList());
        
        log.debug("User stats retrieved: totalEvents={}, totalRevenue={}", userEvents.size(), totalRevenue);
        return stats;
    }
    
    /**
     * Retrieves system-wide statistics across all analytics data.
     * 
     * Aggregates analytics data from all users and rental events to provide
     * platform-wide metrics including total revenue, total number of events,
     * and overall system performance indicators.
     * 
     * @return map containing system-wide statistics for all events and revenue
     */
    public Map<String, Object> getSystemStats() {
        log.debug("Retrieving system statistics");
        
        BigDecimal totalRevenue = analyticsEventRepository.getTotalRevenue();
        Long totalEvents = analyticsEventRepository.getTotalEvents();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalEvents", totalEvents);
        stats.put("averageTransactionValue", totalEvents > 0 
            ? totalRevenue.divide(BigDecimal.valueOf(totalEvents), 2, java.math.RoundingMode.HALF_UP)
            : BigDecimal.ZERO);
        
        log.debug("System stats retrieved: totalRevenue={}, totalEvents={}", totalRevenue, totalEvents);
        return stats;
    }
    
    /**
     * Converts an AnalyticsEvent entity to a response DTO.
     * 
     * @param event the analytics event entity to convert
     * @return the analytics DTO
     */
    private AnalyticsDTO toDTO(AnalyticsEvent event) {
        return new AnalyticsDTO(
            event.getId(),
            event.getRentalId(),
            event.getUserId(),
            event.getAmount(),
            event.getEventType(),
            event.getDescription(),
            event.getCreatedAt()
        );
    }
}
