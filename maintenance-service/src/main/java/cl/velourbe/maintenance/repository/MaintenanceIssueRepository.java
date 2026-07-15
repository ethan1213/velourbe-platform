package cl.velourbe.maintenance.repository;

import cl.velourbe.maintenance.model.entity.MaintenanceIssue;
import cl.velourbe.maintenance.model.enums.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for maintenance issue persistence.
 */
public interface MaintenanceIssueRepository extends JpaRepository<MaintenanceIssue, Long> {

    List<MaintenanceIssue> findByScooterIdOrderByCreatedAtDesc(Long scooterId);

    List<MaintenanceIssue> findByStatusOrderByCreatedAtDesc(IssueStatus status);

    List<MaintenanceIssue> findAllByOrderByCreatedAtDesc();
}
