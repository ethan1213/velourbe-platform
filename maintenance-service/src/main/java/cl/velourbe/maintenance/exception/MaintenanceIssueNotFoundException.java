package cl.velourbe.maintenance.exception;

/**
 * Exception thrown when a maintenance issue cannot be found.
 */
public class MaintenanceIssueNotFoundException extends RuntimeException {

    public MaintenanceIssueNotFoundException(Long id) {
        super("Maintenance issue not found with id: " + id);
    }
}
