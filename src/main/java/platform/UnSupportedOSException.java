package platform;

public class UnSupportedOSException extends RuntimeException {
    public UnSupportedOSException(String message) {
        super(message);
    }

    public UnSupportedOSException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnSupportedOSException(Throwable cause) {
        super(cause);
    }
}
