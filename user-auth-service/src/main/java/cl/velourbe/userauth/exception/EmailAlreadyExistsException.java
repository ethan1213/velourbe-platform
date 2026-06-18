package cl.velourbe.userauth.exception;

/**
 * Excepción lanzada cuando se intenta registrar un email que ya existe en el sistema.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    /**
     * @param email dirección de correo duplicada
     */
    public EmailAlreadyExistsException(String email) {
        super("Email already registered: " + email);
    }
}
