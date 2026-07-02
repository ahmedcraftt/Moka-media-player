package infrastructure.scanner;

import java.io.IOException;

public class MediaScanException extends RuntimeException {
    public MediaScanException(String message) {
        super(message);
    }

    public MediaScanException(Exception e) {
        super(e);
    }
}
