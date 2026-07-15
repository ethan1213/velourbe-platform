package cl.velourbe.maintenance.service;

import cl.velourbe.maintenance.exception.InvalidMaintenanceIssueException;
import cl.velourbe.maintenance.exception.MaintenanceIssueNotFoundException;
import cl.velourbe.maintenance.model.dto.MaintenanceIssueDTO;
import cl.velourbe.maintenance.model.entity.MaintenanceIssue;
import cl.velourbe.maintenance.model.enums.IssueStatus;
import cl.velourbe.maintenance.repository.MaintenanceIssueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for MaintenanceService.
 */
@ExtendWith(MockitoExtension.class)
class MaintenanceServiceTest {

    @Mock
    private MaintenanceIssueRepository repository;

    private MaintenanceService service;

    @BeforeEach
    void setUp() {
        service = new MaintenanceService(repository);
    }

    @Test
    void createIssue_persists_reported_issue() {
        MaintenanceIssueDTO request = new MaintenanceIssueDTO(
                null, 12L, "BRAKE", "Brake does not respond", null, null, null, null, null);

        when(repository.save(any(MaintenanceIssue.class))).thenAnswer(inv -> {
            MaintenanceIssue issue = inv.getArgument(0);
            issue.setId(1L);
            return issue;
        });

        MaintenanceIssueDTO response = service.createIssue(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(12L, response.scooterId());
        assertEquals(IssueStatus.REPORTED.name(), response.status());
        verify(repository).save(any(MaintenanceIssue.class));
    }

    @Test
    void createIssue_throws_when_scooter_id_invalid() {
        MaintenanceIssueDTO request = new MaintenanceIssueDTO(
                null, 0L, "BRAKE", "Brake does not respond", null, null, null, null, null);

        assertThrows(InvalidMaintenanceIssueException.class, () -> service.createIssue(request));
    }

    @Test
    void getIssueById_returns_issue_when_found() {
        MaintenanceIssue issue = issue(IssueStatus.REPORTED);
        when(repository.findById(1L)).thenReturn(Optional.of(issue));

        MaintenanceIssueDTO response = service.getIssueById(1L);

        assertEquals(1L, response.id());
        assertEquals("BRAKE", response.issueType());
    }

    @Test
    void getIssueById_throws_when_missing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MaintenanceIssueNotFoundException.class, () -> service.getIssueById(99L));
    }

    @Test
    void getAllIssues_returns_ordered_list_from_repository() {
        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(issue(IssueStatus.REPORTED)));

        List<MaintenanceIssueDTO> issues = service.getAllIssues();

        assertEquals(1, issues.size());
    }

    @Test
    void getIssuesByScooter_validates_scooter_id() {
        assertThrows(InvalidMaintenanceIssueException.class, () -> service.getIssuesByScooter(-1L));
    }

    @Test
    void markInReview_updates_status() {
        MaintenanceIssue issue = issue(IssueStatus.REPORTED);
        when(repository.findById(1L)).thenReturn(Optional.of(issue));
        when(repository.save(issue)).thenReturn(issue);

        MaintenanceIssueDTO response = service.markInReview(1L);

        assertEquals(IssueStatus.IN_REVIEW.name(), response.status());
    }

    @Test
    void startWork_updates_status() {
        MaintenanceIssue issue = issue(IssueStatus.IN_REVIEW);
        when(repository.findById(1L)).thenReturn(Optional.of(issue));
        when(repository.save(issue)).thenReturn(issue);

        MaintenanceIssueDTO response = service.startWork(1L);

        assertEquals(IssueStatus.IN_PROGRESS.name(), response.status());
    }

    @Test
    void resolveIssue_requires_notes() {
        assertThrows(InvalidMaintenanceIssueException.class, () -> service.resolveIssue(1L, " "));
    }

    @Test
    void resolveIssue_sets_resolved_status_and_notes() {
        MaintenanceIssue issue = issue(IssueStatus.IN_PROGRESS);
        when(repository.findById(1L)).thenReturn(Optional.of(issue));
        when(repository.save(issue)).thenReturn(issue);

        MaintenanceIssueDTO response = service.resolveIssue(1L, "Brake cable replaced");

        assertEquals(IssueStatus.RESOLVED.name(), response.status());
        assertEquals("Brake cable replaced", response.resolutionNotes());
        assertNotNull(response.resolvedAt());
    }

    @Test
    void workflow_throws_when_issue_closed() {
        MaintenanceIssue issue = issue(IssueStatus.CLOSED);
        when(repository.findById(1L)).thenReturn(Optional.of(issue));

        assertThrows(InvalidMaintenanceIssueException.class, () -> service.startWork(1L));
    }

    @Test
    void closeIssue_sets_closed_status() {
        MaintenanceIssue issue = issue(IssueStatus.RESOLVED);
        when(repository.findById(1L)).thenReturn(Optional.of(issue));
        when(repository.save(issue)).thenReturn(issue);

        MaintenanceIssueDTO response = service.closeIssue(1L);

        assertEquals(IssueStatus.CLOSED.name(), response.status());
    }

    private MaintenanceIssue issue(IssueStatus status) {
        MaintenanceIssue issue = new MaintenanceIssue();
        issue.setId(1L);
        issue.setScooterId(12L);
        issue.setIssueType("BRAKE");
        issue.setDescription("Brake does not respond");
        issue.setStatus(status);
        return issue;
    }
}
