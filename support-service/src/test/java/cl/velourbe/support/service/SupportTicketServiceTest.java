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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SupportTicketService.
 * Uses Mockito only — no Spring context.
 */
@ExtendWith(MockitoExtension.class)
class SupportTicketServiceTest {

    @Mock
    private SupportTicketRepository repository;

    private SupportTicketService service;

    @BeforeEach
    void setUp() {
        service = new SupportTicketService(repository);
    }

    @Test
    void create_persists_and_returns_dto() {
        Long userId = 3L;
        CreateTicketRequestDTO dto = new CreateTicketRequestDTO(
                1L, "Scooter stuck", "Battery died mid-ride",
                TicketPriority.HIGH, TicketCategory.SCOOTER);

        when(repository.save(any(SupportTicket.class))).thenAnswer(inv -> {
            SupportTicket t = inv.getArgument(0);
            t.setId(10L);
            return t;
        });

        SupportTicketResponseDTO response = service.create(userId, dto);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals(userId, response.userId());
        assertEquals("Scooter stuck", response.subject());
        assertEquals(TicketStatus.OPEN.name(), response.status());
        verify(repository, times(1)).save(any(SupportTicket.class));
    }

    @Test
    void getById_returns_dto_when_found() {
        SupportTicket t = new SupportTicket();
        t.setId(1L);
        t.setUserId(3L);
        t.setSubject("X");
        t.setDescription("Y");
        t.setStatus(TicketStatus.OPEN);
        t.setPriority(TicketPriority.MEDIUM);
        t.setCategory(TicketCategory.OTHER);

        when(repository.findById(1L)).thenReturn(Optional.of(t));

        SupportTicketResponseDTO response = service.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
    }

    @Test
    void getById_throws_when_missing() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(TicketNotFoundException.class, () -> service.getById(999L));
    }

    @Test
    void assign_sets_in_progress_and_persists() {
        SupportTicket t = new SupportTicket();
        t.setId(1L);
        t.setStatus(TicketStatus.OPEN);
        when(repository.findById(1L)).thenReturn(Optional.of(t));
        when(repository.save(t)).thenReturn(t);

        SupportTicketResponseDTO response = service.assign(1L, "admin@velourbe.cl");

        assertEquals(TicketStatus.IN_PROGRESS.name(), response.status());
        assertEquals("admin@velourbe.cl", response.assignedTo());
    }

    @Test
    void assign_throws_when_blank() {
        assertThrows(InvalidTicketException.class, () -> service.assign(1L, "  "));
    }

    @Test
    void assign_throws_when_ticket_closed() {
        SupportTicket t = new SupportTicket();
        t.setId(1L);
        t.setStatus(TicketStatus.CLOSED);
        when(repository.findById(1L)).thenReturn(Optional.of(t));

        assertThrows(InvalidTicketException.class, () -> service.assign(1L, "admin@x.cl"));
    }

    @Test
    void resolve_sets_resolved_and_persists() {
        SupportTicket t = new SupportTicket();
        t.setId(1L);
        t.setStatus(TicketStatus.IN_PROGRESS);
        when(repository.findById(1L)).thenReturn(Optional.of(t));
        when(repository.save(t)).thenReturn(t);

        SupportTicketResponseDTO response = service.resolve(1L, "Fixed battery");

        assertEquals(TicketStatus.RESOLVED.name(), response.status());
        assertEquals("Fixed battery", response.resolutionNotes());
    }

    @Test
    void close_sets_closed_and_persists() {
        SupportTicket t = new SupportTicket();
        t.setId(1L);
        t.setStatus(TicketStatus.RESOLVED);
        when(repository.findById(1L)).thenReturn(Optional.of(t));
        when(repository.save(t)).thenReturn(t);

        SupportTicketResponseDTO response = service.close(1L);

        assertEquals(TicketStatus.CLOSED.name(), response.status());
    }

    @Test
    void getMyTickets_returns_user_tickets() {
        SupportTicket t1 = new SupportTicket();
        t1.setId(1L);
        t1.setUserId(3L);
        t1.setSubject("A");
        t1.setDescription("a");
        t1.setStatus(TicketStatus.OPEN);
        t1.setPriority(TicketPriority.LOW);
        t1.setCategory(TicketCategory.OTHER);

        when(repository.findByUserIdOrderByCreatedAtDesc(3L)).thenReturn(List.of(t1));

        List<SupportTicketResponseDTO> tickets = service.getMyTickets(3L);

        assertEquals(1, tickets.size());
        assertEquals(3L, tickets.get(0).userId());
    }

    @Test
    void getAll_returns_all_tickets() {
        SupportTicket t1 = new SupportTicket();
        t1.setId(1L);
        t1.setSubject("A");
        t1.setDescription("a");
        t1.setStatus(TicketStatus.OPEN);
        t1.setPriority(TicketPriority.LOW);
        t1.setCategory(TicketCategory.OTHER);

        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(t1));

        List<SupportTicketResponseDTO> tickets = service.getAll();

        assertEquals(1, tickets.size());
    }

    @Test
    void update_applies_assignedTo_and_notes() {
        SupportTicket t = new SupportTicket();
        t.setId(1L);
        t.setStatus(TicketStatus.OPEN);
        when(repository.findById(1L)).thenReturn(Optional.of(t));
        when(repository.save(t)).thenReturn(t);

        UpdateTicketRequestDTO dto = new UpdateTicketRequestDTO("admin@x.cl", "looking into it");
        SupportTicketResponseDTO response = service.update(1L, dto);

        assertEquals("admin@x.cl", response.assignedTo());
        assertEquals("looking into it", response.resolutionNotes());
        assertEquals(TicketStatus.IN_PROGRESS.name(), response.status());
    }
}
