package cl.velourbe.support.repository;

import cl.velourbe.support.model.entity.SupportTicket;
import cl.velourbe.support.model.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for SupportTicket operations.
 */
@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    /**
     * Finds all tickets for a specific user, ordered by most recent first.
     */
    List<SupportTicket> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Finds all tickets with a given status, ordered by most recent first.
     */
    List<SupportTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status);

    /**
     * Returns all tickets, ordered by most recent first.
     */
    List<SupportTicket> findAllByOrderByCreatedAtDesc();
}
