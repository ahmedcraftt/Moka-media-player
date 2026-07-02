package config;

import platform.ArchDetector;
import platform.CpuArch;
import platform.OS;
import platform.OSDetector;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class VlcConfig {

    private VlcConfig() {
    }

    public static void init() {
        OS os = OSDetector.getOS();
        CpuArch arch = ArchDetector.getArch();

        String relativePath = switch (os) {
            case WINDOWS -> switch (arch) {
                case ARM32, ARM64 -> "natives/windows/Arm";
                case x86_64 -> "natives/windows/X86_64";
                default -> "natives/windows/X86_32";
            };

            case LINUX -> switch (arch) {
                case ARM32, ARM64 -> "natives/linux/Arm";
                case x86_64 -> "natives/linux/X86_64";
                default -> "natives/linux/X86_32";
            };

            case MAC -> switch (arch) {
                case ARM32, ARM64 -> "natives/mac/apple-silicon";
                default -> "natives/mac/intel";
            };

            default -> throw new UnsupportedOperationException("Unsupported operating system.");
        };

        try {
            // Folder containing the JAR
            Path appDir = Path.of(
                    VlcConfig.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            ).getParent();

            Path vlcPath = appDir.resolve(relativePath);

            if (Files.exists(vlcPath.resolve("plugins"))) {
                System.setProperty("jna.library.path", vlcPath.toString());
                System.setProperty("VLC_PLUGIN_PATH",
                        vlcPath.resolve("plugins").toString());

                System.out.println("✅ Using bundled VLC:");
                System.out.println("   " + vlcPath);
            } else {
                System.out.println("⚠ Bundled VLC not found.");
                System.out.println("Falling back to system VLC installation.");
            }

        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to locate application directory.", e);
        }
    }
}