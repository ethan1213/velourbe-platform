package cl.velourbe.rental.exception;

public class ScooterNotAvailableException extends RuntimeException {
    public ScooterNotAvailableException(Long id) {
        super("Patineta no disponible: " + id);
    }
}
