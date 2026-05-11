package platform;

public class OSDetector {

    public static OS getOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return OS.WINDOWS;
        if (os.contains("nix")||os.contains("nux")||os.contains("aix")) return OS.LINUX;
        if (os.contains("mac")) return OS.MAC;
        return OS.UNKNOWN;
    }
}
