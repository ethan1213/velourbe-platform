package cl.velourbe.rental.exception;

/**
 * Excepción lanzada cuando se intenta arrendar una patineta que no está disponible
 * (estado IN_USE o MAINTENANCE).
 */
public class ScooterNotAvailableException extends RuntimeException {

    /**
     * @param id identificador de la patineta no disponible
     */
    public ScooterNotAvailableException(Long id) {
        super("Scooter not available: " + id);
    }
}
