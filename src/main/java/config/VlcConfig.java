package config;

import platform.ArchDetector;
import platform.CpuArch;
import platform.OS;
import platform.OSDetector;

import java.nio.file.Files;
import java.nio.file.Path;

public class VlcConfig {

    public static void init() {
        OS os = OSDetector.getOS();
        CpuArch arch = ArchDetector.getArch();
        String basePath;
        switch (os) {
            case WINDOWS -> {
                if (arch == CpuArch.ARM32 || arch == CpuArch.ARM64) {
                    basePath = "natives/windows/Arm";
                } else if (arch == CpuArch.x86_64) {
                    basePath = "natives/windows/X86_64";
                } else {
                    basePath = "natives/windows/X86_32";
                }
            }
            case LINUX -> basePath = "natives/linux";
            case MAC -> {
                if (arch == CpuArch.ARM32 || arch == CpuArch.ARM64) {
                    basePath = "natives/mac/apple-silicon";
                } else basePath = "natives/mac/intel";
            }

            default -> throw new RuntimeException("Unsupported OS for VLC");
        }

        Path path = Path.of(basePath).toAbsolutePath();

        if (!Files.exists(path)) {
            throw new RuntimeException("VLC natives not found: " + path);
        }

        System.setProperty("jna.library.path", path.toString());

        System.setProperty("VLC_PLUGIN_PATH", path.resolve("plugins").toString());

        System.out.println("✅ VLC loaded from: " + path);
    }
}