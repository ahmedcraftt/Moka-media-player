package config;

import platform.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class VLCConfig {

    private static final Logger logger = LoggerFactory.getLogger(VLCConfig.class);

    private static OS os = OSDetector.getOS();

    private static CpuArch arch = ArchDetector.getArch();

    private static final String DEFAULT_NATIVES_PATH = switch (os) {
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

        default -> throw new UnSupportedOSException("Unsupported operating system.");
    };

    private static String vlcNativesPath = DEFAULT_NATIVES_PATH;

    private VLCConfig() {
    }

    public static void init() {

        try {
            Path appDir = Path.of(
                    VLCConfig.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            ).getParent();

            Path vlcPath = appDir.resolve(vlcNativesPath);

            if (Files.exists(vlcPath.resolve("plugins"))) {
                System.setProperty("jna.library.path", vlcPath.toString());
                System.setProperty("VLC_PLUGIN_PATH", vlcPath.resolve("plugins").toString());

                logger.info("Using bundled VLC binaries resolved at: {}", vlcPath);
            } else {
                logger.warn("Bundled VLC binaries not found at layout: {}. Falling back to standard system VLC paths.", vlcPath);
            }

        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to locate application directory.", e);
        }

    }
}