package cl.velourbe.support.service;

import cl.velourbe.support.exception.InvalidTicketException;
import cl.velourbe.support.exception.TicketNotFoundException;
import cl.velourbe.support.model.dto.CreateTicketRequestDTO;
import cl.velourbe.support.model.dto.SupportTicketResponseDTO;
import cl.velourbe.support.model.dto.UpdateTicketRequestDTO;
import cl.velourbe.support.model.entity.SupportTicket;
import cl.velourbe.support.model.enums.TicketCategory;
import cl.velourbe.support.model.enums.TicketPriority;
import cl.velourbe.support.model.enums.TicketStatus;
import cl.velourbe.support.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for support ticket operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupportTicketService {

    private final SupportTicketRepository repository;

    /**
     * Creates a new support ticket for the given user.
     */
    @Transactional
    public SupportTicketResponseDTO create(Long userId, CreateTicketRequestDTO dto) {
        log.info("Creando ticket para userId={} subject='{}'", userId, dto.subject());
        SupportTicket t = SupportTicket.builder()
                .userId(userId)
                .rentalId(dto.rentalId())
                .subject(dto.subject())
                .description(dto.description())
                .status(TicketStatus.OPEN)
                .priority(dto.priority() != null ? dto.priority() : TicketPriority.MEDIUM)
                .category(dto.category() != null ? dto.category() : TicketCategory.OTHER)
                .build();
        return toDTO(repository.save(t));
    }

    /**
     * Returns a ticket by id, or throws if not found.
     */
    public SupportTicketResponseDTO getById(Long id) {
        return toDTO(repository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id)));
    }

    /**
     * Returns all tickets created by the given user, most recent first.
     */
    public List<SupportTicketResponseDTO> getMyTickets(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDTO).toList();
    }

    /**
     * Returns all tickets in the system, most recent first.
     */
    public List<SupportTicketResponseDTO> getAll() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toDTO).toList();
    }

    /**
     * Assigns a ticket to an admin. Sets status to IN_PROGRESS.
     */
    @Transactional
    public SupportTicketResponseDTO assign(Long id, String assignedTo) {
        if (assignedTo == null || assignedTo.isBlank()) {
            throw new InvalidTicketException("assignedTo es requerido");
        }
        SupportTicket t = mustFind(id);
        if (t.getStatus() == TicketStatus.CLOSED) {
            throw new InvalidTicketException("No se puede asignar un ticket cerrado");
        }
        t.setAssignedTo(assignedTo);
        t.setStatus(TicketStatus.IN_PROGRESS);
        log.info("Ticket {} asignado a {}", id, assignedTo);
        return toDTO(repository.save(t));
    }

    /**
     * Marks a ticket as resolved with the given notes.
     */
    @Transactional
    public SupportTicketResponseDTO resolve(Long id, String notes) {
        SupportTicket t = mustFind(id);
        t.setStatus(TicketStatus.RESOLVED);
        t.setResolutionNotes(notes);
        log.info("Ticket {} resuelto", id);
        return toDTO(repository.save(t));
    }

    /**
     * Closes a ticket.
     */
    @Transactional
    public SupportTicketResponseDTO close(Long id) {
        SupportTicket t = mustFind(id);
        t.setStatus(TicketStatus.CLOSED);
        log.info("Ticket {} cerrado", id);
        return toDTO(repository.save(t));
    }

    /**
     * Convenience overload for the controller PATCH that only updates notes/assignment.
     */
    @Transactional
    public SupportTicketResponseDTO update(Long id, UpdateTicketRequestDTO dto) {
        SupportTicket t = mustFind(id);
        if (dto.assignedTo() != null && !dto.assignedTo().isBlank()) {
            if (t.getStatus() == TicketStatus.CLOSED) {
                throw new InvalidTicketException("No se puede asignar un ticket cerrado");
            }
            t.setAssignedTo(dto.assignedTo());
            if (t.getStatus() == TicketStatus.OPEN) {
                t.setStatus(TicketStatus.IN_PROGRESS);
            }
        }
        if (dto.resolutionNotes() != null) {
            t.setResolutionNotes(dto.resolutionNotes());
        }
        return toDTO(repository.save(t));
    }

    private SupportTicket mustFind(Long id) {
        return repository.findById(id).orElseThrow(() -> new TicketNotFoundException(id));
    }

    private SupportTicketResponseDTO toDTO(SupportTicket t) {
        return new SupportTicketResponseDTO(
                t.getId(), t.getUserId(), t.getRentalId(),
                t.getSubject(), t.getDescription(),
                t.getStatus() != null ? t.getStatus().name() : null,
                t.getPriority() != null ? t.getPriority().name() : null,
                t.getCategory() != null ? t.getCategory().name() : null,
                t.getAssignedTo(), t.getResolutionNotes(),
                t.getCreatedAt(), t.getUpdatedAt());
    }
}
