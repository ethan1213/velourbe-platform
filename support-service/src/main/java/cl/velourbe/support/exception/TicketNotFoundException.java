package cl.velourbe.support.exception;

/**
 * Thrown when a support ticket is not found by id.
 */
public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(Long id) {
        super("Ticket no encontrado: " + id);
    }
}
