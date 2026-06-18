package cl.velourbe.userauth.exception;

/**
 * Excepción lanzada cuando el email o la contraseña proporcionados son incorrectos.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
