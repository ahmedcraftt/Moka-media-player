package config;

import platform.OS;
import platform.OSDetector;

import java.nio.file.Files;
import java.nio.file.Path;

public class VlcConfig {

    public static void init() {
        OS os = OSDetector.getOS();

        String basePath = switch (os) {
            case WINDOWS -> "natives/windows";
            case LINUX -> "natives/linux";
            case MAC -> "natives/mac"; //not supported yet
            default -> throw new RuntimeException("Unsupported OS for VLC");
        };

        Path path = Path.of(basePath).toAbsolutePath();

        if (!Files.exists(path)) {
            throw new RuntimeException("VLC natives not found: " + path);
        }

        System.setProperty("jna.library.path", path.toString());

        System.setProperty("VLC_PLUGIN_PATH", path.resolve("plugins").toString());

        System.out.println("✅ VLC loaded from: " + path);
    }
}