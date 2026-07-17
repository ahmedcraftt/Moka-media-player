package infrastructure.storage;

import platform.OS;
import platform.OSDetector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppDirectories {

    public static Path getDataDirectory() throws IOException {
        OS os = OSDetector.getOS();

        Path dir;

        if (os.equals(OS.WINDOWS)) {
            dir = Paths.get(
                    System.getenv("LOCALAPPDATA"),
                    "Moka"
            );
        } else if (os.equals(OS.MAC)) {
            dir = Paths.get(
                    System.getProperty("user.home"),
                    "Library",
                    "Application Support",
                    "Moka"
            );
        } else {
            String xdg = System.getenv("XDG_DATA_HOME");

            dir = (xdg != null && !xdg.isBlank())
                    ? Paths.get(xdg, "Moka")
                    : Paths.get(
                    System.getProperty("user.home"),
                    ".local",
                    "share",
                    "Moka");
        }

        Files.createDirectories(dir);

        return dir;
    }

    private AppDirectories() {
    }
}
