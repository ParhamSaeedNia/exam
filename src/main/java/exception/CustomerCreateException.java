package exception;

public class CustomerCreateException extends Exception {
    public CustomerCreateException(String message) {
        super(message);
    }

    public CustomerCreateException(String message, Throwable cause) {
        super(message, cause);
    }
}
