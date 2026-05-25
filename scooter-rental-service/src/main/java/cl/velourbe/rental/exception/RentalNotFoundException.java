package cl.velourbe.rental.exception;

public class RentalNotFoundException extends RuntimeException {
    public RentalNotFoundException(Long id) {
        super("Arriendo no encontrado: " + id);
    }
}
