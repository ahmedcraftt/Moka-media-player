package gui.main;

public class MainLauncher {
    public static final long START_TIME = System.nanoTime();

    public static void main(String[] args) {
        MainApplication.launch(MainApplication.class, args);
    }
}
