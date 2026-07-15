package cl.velourbe.maintenance.model.enums;

/**
 * Issue status enumeration.
 * Represents the lifecycle states of a maintenance issue.
 */
public enum IssueStatus {
    REPORTED,   // Issue has been reported
    IN_REVIEW,  // Issue is being reviewed
    IN_PROGRESS, // Maintenance is in progress
    RESOLVED,   // Issue has been resolved
    CLOSED      // Issue has been closed
}
