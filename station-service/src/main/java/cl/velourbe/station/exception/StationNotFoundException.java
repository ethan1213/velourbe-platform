package cl.velourbe.station.exception;

/**
 * Thrown when a station does not exist.
 */
public class StationNotFoundException extends RuntimeException {

    public StationNotFoundException(Long id) {
        super("Estación no encontrada: id=" + id);
    }
}
