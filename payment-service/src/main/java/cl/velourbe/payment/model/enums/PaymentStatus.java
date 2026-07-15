package cl.velourbe.payment.model.enums;

/**
 * Enumeration of possible payment statuses.
 */
public enum PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED,
    CANCELLED
}
