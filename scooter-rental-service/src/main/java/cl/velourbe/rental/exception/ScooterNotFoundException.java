package cl.velourbe.rental.exception;

/**
 * Excepción lanzada cuando no se encuentra una patineta con el ID solicitado.
 */
public class ScooterNotFoundException extends RuntimeException {

    /**
     * @param id identificador de la patineta no encontrada
     */
    public ScooterNotFoundException(Long id) {
        super("Scooter not found: " + id);
    }
}
