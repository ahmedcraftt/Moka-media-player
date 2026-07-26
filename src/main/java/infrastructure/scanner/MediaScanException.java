package infrastructure.scanner;

public class MediaScanException extends RuntimeException {
    public MediaScanException(String message) {
        super(message);
    }

    public MediaScanException(String message, Throwable cause) {
        super(message, cause);
    }

    public MediaScanException(Throwable cause) {
        super(cause);
    }

}
