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
    private final ObjectMapper objectMapper;

    public BffService(@Qualifier("userAuthClient") WebClient userAuthClient,
                      @Qualifier("rentalClient") WebClient rentalClient,
                      ObjectMapper objectMapper) {
        this.userAuthClient = userAuthClient;
        this.rentalClient = rentalClient;
        this.objectMapper = objectMapper;
    }

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

    private <T> List<T> extractEmbeddedList(JsonNode json, String embeddedKey,
                                             TypeReference<List<T>> typeRef) {
        if (json == null) return List.of();
        JsonNode embedded = json.path("_embedded").path(embeddedKey);
        if (embedded.isMissingNode() || embedded.isNull()) return List.of();
        return objectMapper.convertValue(embedded, typeRef);
    }
}
