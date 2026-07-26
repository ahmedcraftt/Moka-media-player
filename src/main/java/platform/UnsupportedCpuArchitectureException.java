package platform;

public class UnsupportedCpuArchitectureException extends RuntimeException {
    public UnsupportedCpuArchitectureException(String message) {
        super(message);
    }

    public UnsupportedCpuArchitectureException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedCpuArchitectureException(Throwable cause) {
        super(cause);
    }
}
