package cl.velourbe.station.exception;

/**
 * Thrown when a station operation is invalid (e.g., docking at a full station).
 */
public class InvalidStationException extends RuntimeException {

    public InvalidStationException(String message) {
        super(message);
    }
}
