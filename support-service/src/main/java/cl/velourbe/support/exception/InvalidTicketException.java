package cl.velourbe.support.exception;

/**
 * Thrown when a ticket operation is invalid (e.g. closing a non-existent ticket,
 * assigning a closed one, etc.).
 */
public class InvalidTicketException extends RuntimeException {
    public InvalidTicketException(String message) {
        super(message);
    }
}
