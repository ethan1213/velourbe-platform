package cl.velourbe.bff.service;

import cl.velourbe.bff.dto.*;
import cl.velourbe.bff.security.SecurityUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BffService {

    private final WebClient userAuthClient;
    private final WebClient rentalClient;
    private final WebClient supportClient;
    private final WebClient maintenanceClient;
    private final ObjectMapper objectMapper;

    public BffService(@Qualifier("userAuthClient") WebClient userAuthClient,
                      @Qualifier("rentalClient") WebClient rentalClient,
                      @Qualifier("supportClient") WebClient supportClient,
                      @Qualifier("maintenanceClient") WebClient maintenanceClient,
                      ObjectMapper objectMapper) {
        this.userAuthClient = userAuthClient;
        this.rentalClient = rentalClient;
        this.supportClient = supportClient;
        this.maintenanceClient = maintenanceClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Builds the authenticated user's dashboard by aggregating profile and rental data.
     *
     * @param bearerToken JWT bearer token received by the BFF
     * @return dashboard response containing profile, active rentals, and recent rentals
     */
    public DashboardResponseDTO getDashboard(String bearerToken) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Construyendo dashboard para userId={}", userId);

        UserProfileDTO profile = fetchUserProfile(userId, bearerToken);
        List<RentalDTO> allRentals = fetchMyRentals(bearerToken);
        List<RentalDTO> active = allRentals.stream()
                .filter(r -> "ACTIVE".equals(r.getStatus())).collect(Collectors.toList());
        List<RentalDTO> recent = allRentals.stream()
                .filter(r -> "COMPLETED".equals(r.getStatus()))
                .limit(5).collect(Collectors.toList());

        return new DashboardResponseDTO(profile, active, recent);
    }

    /**
     * Retrieves currently available scooters from scooter-rental-service.
     *
     * @param bearerToken JWT bearer token received by the BFF
     * @return list of available scooters
     * @throws WebClientResponseException when scooter-rental-service returns an HTTP error
     */
    public List<AvailableScooterDTO> getAvailableScooters(String bearerToken) {
        log.info("Consultando scooters disponibles");
        try {
            JsonNode json = rentalClient.get()
                    .uri("/api/scooters/available")
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            return extractEmbeddedList(json, "scooterResponseDTOList",
                    new TypeReference<List<AvailableScooterDTO>>() {});
        } catch (WebClientResponseException ex) {
            log.error("Error al obtener scooters disponibles: {}", ex.getMessage());
            throw ex;
        }
    }

    /**
     * Calculates a rental summary for the authenticated user using rental-service data.
     *
     * @param bearerToken JWT bearer token received by the BFF
     * @return summarized rental counts and total minutes
     */
    public RentalSummaryDTO getRentalSummary(String bearerToken) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Calculando resumen de arriendos para userId={}", userId);

        List<RentalDTO> rentals = fetchMyRentals(bearerToken);
        int total = rentals.size();
        int completed = (int) rentals.stream().filter(r -> "COMPLETED".equals(r.getStatus())).count();
        int active = (int) rentals.stream().filter(r -> "ACTIVE".equals(r.getStatus())).count();
        int totalMinutes = rentals.stream()
                .filter(r -> r.getTotalMinutes() != null)
                .mapToInt(RentalDTO::getTotalMinutes).sum();

        return new RentalSummaryDTO(userId, total, completed, active, totalMinutes);
    }

    /**
     * Retrieves maintenance issues from maintenance-service for administrative dashboard views.
     *
     * @param bearerToken JWT bearer token received by the BFF
     * @return list of maintenance issues from the maintenance microservice
     * @throws WebClientResponseException when maintenance-service returns an HTTP error
     */
    public List<MaintenanceIssueDTO> getMaintenanceIssues(String bearerToken) {
        log.info("Consultando incidencias de mantenimiento");
        try {
            JsonNode json = maintenanceClient.get()
                    .uri("/api/maintenance/issues")
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            return extractEmbeddedList(json, "maintenanceIssueDTOList",
                    new TypeReference<List<MaintenanceIssueDTO>>() {});
        } catch (WebClientResponseException ex) {
            log.error("Error al obtener incidencias de mantenimiento: {}", ex.getMessage());
            throw ex;
        }
    }

    /**
     * Fetches the current user's profile from user-auth-service.
     *
     * @param userId user identifier extracted from the JWT
     * @param bearerToken JWT bearer token received by the BFF
     * @return user profile, or null when the upstream profile lookup fails
     */
    private UserProfileDTO fetchUserProfile(Long userId, String bearerToken) {
        try {
            return userAuthClient.get()
                    .uri("/api/users/{id}", userId)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(UserProfileDTO.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.warn("No se pudo obtener perfil de userId={}: {}", userId, ex.getMessage());
            return null;
        }
    }

    /**
     * Fetches the current user's rentals from scooter-rental-service.
     *
     * @param bearerToken JWT bearer token received by the BFF
     * @return user rental list, or an empty list when the upstream call fails
     */
    private List<RentalDTO> fetchMyRentals(String bearerToken) {
        try {
            JsonNode json = rentalClient.get()
                    .uri("/api/rentals/my")
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            return extractEmbeddedList(json, "rentalResponseDTOList",
                    new TypeReference<List<RentalDTO>>() {});
        } catch (WebClientResponseException ex) {
            log.error("Error al obtener arriendos: {}", ex.getMessage());
            return List.of();
        }
    }

    /**
     * Extracts a typed list from a Spring HATEOAS CollectionModel response.
     *
     * @param json upstream JSON response
     * @param embeddedKey key inside the _embedded object
     * @param typeRef target list type
     * @return extracted list, or an empty list when the embedded collection is absent
     */
    private <T> List<T> extractEmbeddedList(JsonNode json, String embeddedKey,
                                             TypeReference<List<T>> typeRef) {
        if (json == null) return List.of();
        JsonNode embedded = json.path("_embedded").path(embeddedKey);
        if (embedded.isMissingNode() || embedded.isNull()) return List.of();
        return objectMapper.convertValue(embedded, typeRef);
    }
}
