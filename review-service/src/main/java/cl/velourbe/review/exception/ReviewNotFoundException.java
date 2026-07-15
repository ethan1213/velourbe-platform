package cl.velourbe.review.exception;

/**
 * Thrown when a review does not exist.
 */
public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException(Long id) {
        super("Reseña no encontrada: id=" + id);
    }
}
