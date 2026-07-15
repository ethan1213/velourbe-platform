package cl.velourbe.analytics.controller;

import cl.velourbe.analytics.model.dto.AnalyticsDTO;
import cl.velourbe.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * REST Controller for analytics endpoints.
 * Provides endpoints for recording rental events and retrieving analytics statistics.
 * All endpoints require authentication via JWT token.
 */
@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;
    
    /**
     * Records a new rental event in the analytics system.
     * 
     * @param rentalId the rental ID
     * @param amount the rental amount
     * @return created analytics event response with HATEOAS links
     */
    @PostMapping("/rental")
    public ResponseEntity<EntityModel<AnalyticsDTO>> recordRentalEvent(
            @RequestParam Long rentalId,
            @RequestParam BigDecimal amount) {
        Long userId = getCurrentUserId();
        log.info("Recording rental event: rentalId={}, userId={}, amount={}", rentalId, userId, amount);
        
        AnalyticsDTO response = analyticsService.recordRentalEvent(rentalId, userId, amount);
        EntityModel<AnalyticsDTO> model = EntityModel.of(response,
            linkTo(methodOn(AnalyticsController.class).recordRentalEvent(rentalId, amount)).withSelfRel(),
            linkTo(methodOn(AnalyticsController.class).getUserStats(userId)).withRel("userStats"));
        
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }
    
    /**
     * Retrieves statistics for a specific user.
     * 
     * @param userId the user ID (defaults to current authenticated user if not provided)
     * @return user statistics including total events and revenue
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<EntityModel<Map<String, Object>>> getUserStats(@PathVariable Long userId) {
        log.info("Retrieving user statistics for userId={}", userId);
        
        Map<String, Object> stats = analyticsService.getUserStats(userId);
        EntityModel<Map<String, Object>> model = EntityModel.of(stats,
            linkTo(methodOn(AnalyticsController.class).getUserStats(userId)).withSelfRel(),
            linkTo(methodOn(AnalyticsController.class).getSystemStats()).withRel("systemStats"));
        
        return ResponseEntity.ok(model);
    }
    
    /**
     * Retrieves system-wide analytics statistics.
     * 
     * @return system statistics including total revenue and total events
     */
    @GetMapping("/system")
    public ResponseEntity<EntityModel<Map<String, Object>>> getSystemStats() {
        log.info("Retrieving system statistics");
        
        Map<String, Object> stats = analyticsService.getSystemStats();
        EntityModel<Map<String, Object>> model = EntityModel.of(stats,
            linkTo(methodOn(AnalyticsController.class).getSystemStats()).withSelfRel());
        
        return ResponseEntity.ok(model);
    }
    
    /**
     * Extracts the current user ID from the JWT token.
     * 
     * @return the authenticated user ID
     */
    private Long getCurrentUserId() {
        String userIdStr = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();
        return Long.parseLong(userIdStr);
    }
}
