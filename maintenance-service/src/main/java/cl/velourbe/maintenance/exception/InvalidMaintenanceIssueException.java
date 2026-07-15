package cl.velourbe.maintenance.exception;

/**
 * Exception thrown when a maintenance issue request is invalid.
 */
public class InvalidMaintenanceIssueException extends RuntimeException {

    public InvalidMaintenanceIssueException(String message) {
        super(message);
    }
}
