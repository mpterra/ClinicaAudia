package exception;

public class LoginDuplicadoException extends Exception {
    private static final long serialVersionUID = 1L;

	public LoginDuplicadoException(String message) {
        super(message);
    }
}
