package cl.velourbe.bff.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Route table of the BFF: maps each public API prefix to the base URL
 * of the microservice that owns it.
 */
@Component
public class ProxyRoutes {

    private final Map<String, String> routes = new LinkedHashMap<>();

    public ProxyRoutes(@Value("${services.user-auth.url}") String userAuthUrl,
                       @Value("${services.rental.url}") String rentalUrl,
                       @Value("${services.payment.url}") String paymentUrl,
                       @Value("${services.notification.url}") String notificationUrl,
                       @Value("${services.analytics.url}") String analyticsUrl,
                       @Value("${services.logistics.url}") String logisticsUrl,
                       @Value("${services.maintenance.url}") String maintenanceUrl,
                       @Value("${services.support.url}") String supportUrl,
                       @Value("${services.station.url}") String stationUrl,
                       @Value("${services.review.url}") String reviewUrl) {
        routes.put("/api/auth", userAuthUrl);
        routes.put("/api/users", userAuthUrl);
        routes.put("/api/scooters", rentalUrl);
        routes.put("/api/rentals", rentalUrl);
        routes.put("/api/payments", paymentUrl);
        routes.put("/api/notifications", notificationUrl);
        routes.put("/api/analytics", analyticsUrl);
        routes.put("/api/logistics", logisticsUrl);
        routes.put("/api/maintenance", maintenanceUrl);
        routes.put("/api/support", supportUrl);
        routes.put("/api/stations", stationUrl);
        routes.put("/api/reviews", reviewUrl);
    }

    /**
     * Resolves the base URL of the microservice that owns the given path.
     *
     * @param path request path (e.g. /api/rentals/my)
     * @return base URL of the target microservice, or null if no route matches
     */
    public String resolve(String path) {
        return routes.entrySet().stream()
                .filter(e -> path.equals(e.getKey()) || path.startsWith(e.getKey() + "/"))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}
