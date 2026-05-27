package cl.velourbe.rental.exception;

/**
 * Excepción lanzada cuando no se encuentra un arriendo con el ID solicitado.
 */
public class RentalNotFoundException extends RuntimeException {

    /**
     * @param id identificador del arriendo no encontrado
     */
    public RentalNotFoundException(Long id) {
        super("Rental not found: " + id);
    }
}
