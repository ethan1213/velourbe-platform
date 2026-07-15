package cl.velourbe.bff.controller;

import cl.velourbe.bff.dto.*;
import cl.velourbe.bff.service.BffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bff")
@RequiredArgsConstructor
public class BffController {

    private final BffService bffService;

    /**
     * GET /api/bff/dashboard — Panel del usuario autenticado.
     * Agrega: perfil del usuario (user-auth-service) + arriendos activos y recientes (scooter-rental-service).
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponseDTO> dashboard(
            @RequestHeader("Authorization") String authorization) {
        log.info("GET /api/bff/dashboard");
        return ResponseEntity.ok(bffService.getDashboard(authorization));
    }

    /**
     * GET /api/bff/scooters/available — Lista de patinetas disponibles para arrendar.
     * Proxy hacia scooter-rental-service con formato enriquecido.
     */
    @GetMapping("/scooters/available")
    public ResponseEntity<List<AvailableScooterDTO>> availableScooters(
            @RequestHeader("Authorization") String authorization) {
        log.info("GET /api/bff/scooters/available");
        return ResponseEntity.ok(bffService.getAvailableScooters(authorization));
    }

    /**
     * GET /api/bff/rental-summary — Resumen estadístico de arriendos del usuario autenticado.
     * Calcula totales, completados, activos y minutos acumulados.
     */
    @GetMapping("/rental-summary")
    public ResponseEntity<RentalSummaryDTO> rentalSummary(
            @RequestHeader("Authorization") String authorization) {
        log.info("GET /api/bff/rental-summary");
        return ResponseEntity.ok(bffService.getRentalSummary(authorization));
    }

    /**
     * GET /api/bff/maintenance/issues - Administrative view of maintenance issues.
     * Proxies maintenance-service through the BFF and keeps upstream errors centralized.
     */
    @GetMapping("/maintenance/issues")
    public ResponseEntity<List<MaintenanceIssueDTO>> maintenanceIssues(
            @RequestHeader("Authorization") String authorization) {
        log.info("GET /api/bff/maintenance/issues");
        return ResponseEntity.ok(bffService.getMaintenanceIssues(authorization));
    }
}
