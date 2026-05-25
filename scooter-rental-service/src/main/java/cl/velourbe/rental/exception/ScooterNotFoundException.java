package cl.velourbe.rental.exception;

public class ScooterNotFoundException extends RuntimeException {
    public ScooterNotFoundException(Long id) {
        super("Patineta no encontrada: " + id);
    }
}
