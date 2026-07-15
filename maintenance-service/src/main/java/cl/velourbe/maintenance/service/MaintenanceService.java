package cl.velourbe.maintenance.service;

import cl.velourbe.maintenance.exception.InvalidMaintenanceIssueException;
import cl.velourbe.maintenance.exception.MaintenanceIssueNotFoundException;
import cl.velourbe.maintenance.model.dto.MaintenanceIssueDTO;
import cl.velourbe.maintenance.model.entity.MaintenanceIssue;
import cl.velourbe.maintenance.model.enums.IssueStatus;
import cl.velourbe.maintenance.repository.MaintenanceIssueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer for maintenance issue business rules.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceIssueRepository repository;

    /**
     * Creates a new maintenance issue for a scooter.
     *
     * @param dto request data containing scooter id, type, and description
     * @return persisted maintenance issue data
     * @throws InvalidMaintenanceIssueException when scooter id, issue type, or description is invalid
     */
    @Transactional
    public MaintenanceIssueDTO createIssue(MaintenanceIssueDTO dto) {
        validateCreateRequest(dto);
        MaintenanceIssue issue = MaintenanceIssue.builder()
                .scooterId(dto.scooterId())
                .issueType(dto.issueType().trim())
                .description(dto.description().trim())
                .status(IssueStatus.REPORTED)
                .build();
        log.info("Creating maintenance issue for scooterId={} type={}", dto.scooterId(), dto.issueType());
        return toDTO(repository.save(issue));
    }

    /**
     * Returns a maintenance issue by its identifier.
     *
     * @param id maintenance issue identifier
     * @return maintenance issue data
     * @throws MaintenanceIssueNotFoundException when the issue does not exist
     */
    @Transactional(readOnly = true)
    public MaintenanceIssueDTO getIssueById(Long id) {
        return toDTO(mustFind(id));
    }

    /**
     * Returns all maintenance issues ordered from newest to oldest.
     *
     * @return list of maintenance issues
     */
    @Transactional(readOnly = true)
    public List<MaintenanceIssueDTO> getAllIssues() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Returns all maintenance issues for a scooter ordered from newest to oldest.
     *
     * @param scooterId scooter identifier
     * @return list of issues reported for the scooter
     * @throws InvalidMaintenanceIssueException when scooter id is invalid
     */
    @Transactional(readOnly = true)
    public List<MaintenanceIssueDTO> getIssuesByScooter(Long scooterId) {
        validatePositiveId(scooterId, "scooterId");
        return repository.findByScooterIdOrderByCreatedAtDesc(scooterId).stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Returns all maintenance issues with a specific status.
     *
     * @param status issue status to filter by
     * @return list of matching maintenance issues
     * @throws InvalidMaintenanceIssueException when status is null
     */
    @Transactional(readOnly = true)
    public List<MaintenanceIssueDTO> getIssuesByStatus(IssueStatus status) {
        if (status == null) {
            throw new InvalidMaintenanceIssueException("status es requerido");
        }
        return repository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Moves a maintenance issue to IN_REVIEW.
     *
     * @param id maintenance issue identifier
     * @return updated maintenance issue data
     * @throws MaintenanceIssueNotFoundException when the issue does not exist
     * @throws InvalidMaintenanceIssueException when the issue is already closed
     */
    @Transactional
    public MaintenanceIssueDTO markInReview(Long id) {
        MaintenanceIssue issue = mustFind(id);
        ensureNotClosed(issue);
        issue.setStatus(IssueStatus.IN_REVIEW);
        log.info("Maintenance issue {} marked IN_REVIEW", id);
        return toDTO(repository.save(issue));
    }

    /**
     * Moves a maintenance issue to IN_PROGRESS.
     *
     * @param id maintenance issue identifier
     * @return updated maintenance issue data
     * @throws MaintenanceIssueNotFoundException when the issue does not exist
     * @throws InvalidMaintenanceIssueException when the issue is already closed
     */
    @Transactional
    public MaintenanceIssueDTO startWork(Long id) {
        MaintenanceIssue issue = mustFind(id);
        ensureNotClosed(issue);
        issue.setStatus(IssueStatus.IN_PROGRESS);
        log.info("Maintenance issue {} marked IN_PROGRESS", id);
        return toDTO(repository.save(issue));
    }

    /**
     * Resolves a maintenance issue with resolution notes.
     *
     * @param id maintenance issue identifier
     * @param resolutionNotes notes explaining the fix
     * @return updated maintenance issue data
     * @throws MaintenanceIssueNotFoundException when the issue does not exist
     * @throws InvalidMaintenanceIssueException when resolution notes are blank or the issue is closed
     */
    @Transactional
    public MaintenanceIssueDTO resolveIssue(Long id, String resolutionNotes) {
        if (resolutionNotes == null || resolutionNotes.isBlank()) {
            throw new InvalidMaintenanceIssueException("resolutionNotes es requerido");
        }
        MaintenanceIssue issue = mustFind(id);
        ensureNotClosed(issue);
        issue.setStatus(IssueStatus.RESOLVED);
        issue.setResolutionNotes(resolutionNotes.trim());
        issue.setResolvedAt(LocalDateTime.now());
        log.info("Maintenance issue {} resolved", id);
        return toDTO(repository.save(issue));
    }

    /**
     * Closes a maintenance issue after review or resolution.
     *
     * @param id maintenance issue identifier
     * @return updated maintenance issue data
     * @throws MaintenanceIssueNotFoundException when the issue does not exist
     */
    @Transactional
    public MaintenanceIssueDTO closeIssue(Long id) {
        MaintenanceIssue issue = mustFind(id);
        issue.setStatus(IssueStatus.CLOSED);
        log.info("Maintenance issue {} closed", id);
        return toDTO(repository.save(issue));
    }

    /**
     * Finds a maintenance issue or throws the domain not-found exception.
     *
     * @param id maintenance issue identifier
     * @return maintenance issue entity
     */
    private MaintenanceIssue mustFind(Long id) {
        validatePositiveId(id, "id");
        return repository.findById(id)
                .orElseThrow(() -> new MaintenanceIssueNotFoundException(id));
    }

    /**
     * Validates the fields required to create a maintenance issue.
     *
     * @param dto maintenance issue create request
     */
    private void validateCreateRequest(MaintenanceIssueDTO dto) {
        if (dto == null) {
            throw new InvalidMaintenanceIssueException("maintenance issue body es requerido");
        }
        validatePositiveId(dto.scooterId(), "scooterId");
        if (dto.issueType() == null || dto.issueType().isBlank()) {
            throw new InvalidMaintenanceIssueException("issueType es requerido");
        }
        if (dto.description() == null || dto.description().isBlank()) {
            throw new InvalidMaintenanceIssueException("description es requerida");
        }
    }

    /**
     * Validates that an identifier is positive.
     *
     * @param id identifier value
     * @param field field name used in the exception message
     */
    private void validatePositiveId(Long id, String field) {
        if (id == null || id <= 0) {
            throw new InvalidMaintenanceIssueException(field + " debe ser mayor a 0");
        }
    }

    /**
     * Ensures closed issues are not modified by workflow transitions.
     *
     * @param issue maintenance issue entity
     */
    private void ensureNotClosed(MaintenanceIssue issue) {
        if (issue.getStatus() == IssueStatus.CLOSED) {
            throw new InvalidMaintenanceIssueException("No se puede modificar una incidencia cerrada");
        }
    }

    /**
     * Converts a maintenance issue entity to a DTO.
     *
     * @param issue maintenance issue entity
     * @return maintenance issue DTO
     */
    private MaintenanceIssueDTO toDTO(MaintenanceIssue issue) {
        return new MaintenanceIssueDTO(
                issue.getId(),
                issue.getScooterId(),
                issue.getIssueType(),
                issue.getDescription(),
                issue.getStatus() != null ? issue.getStatus().name() : null,
                issue.getResolutionNotes(),
                issue.getCreatedAt(),
                issue.getUpdatedAt(),
                issue.getResolvedAt());
    }
}
