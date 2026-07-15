package cl.velourbe.review.exception;

/**
 * Thrown when a review operation is invalid (e.g., duplicated review for the same rental).
 */
public class InvalidReviewException extends RuntimeException {

    public InvalidReviewException(String message) {
        super(message);
    }
}
