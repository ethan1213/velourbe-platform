package cl.velourbe.payment.exception;

/**
 * Exception thrown when a payment request is invalid.
 */
public class InvalidPaymentException extends RuntimeException {
    public InvalidPaymentException(String message) {
        super(message);
    }
}
