package gui.main;

/**
 * Lightweight bootstrap class responsible for launching the application.
 * <p>
 * Records the application start timestamp before JavaFX initialization so
 * the total startup time can be measured accurately. The timestamp is later
 * consumed by MainApplication for performance logging.
 */

public class MainLauncher {
    public static final long START_TIME = System.nanoTime();

    public static void main(String[] args) {
        MainApplication.launch(MainApplication.class, args);
    }
}
