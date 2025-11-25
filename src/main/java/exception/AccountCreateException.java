package exception;

public class AccountCreateException extends Exception {
    public AccountCreateException(String message) {
        super(message);
    }

    public AccountCreateException(String message, Throwable cause) {
        super(message, cause);
    }
}
