package cl.velourbe.logistics.exception;

/**
 * Exception thrown when a scooter location cannot be found.
 */
public class LocationNotFoundException extends RuntimeException {

    public LocationNotFoundException(Long scooterId) {
        super("Location not found for scooterId: " + scooterId);
    }
}
