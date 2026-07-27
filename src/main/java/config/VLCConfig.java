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
            case x86_32 -> "natives/windows/X86_32";
            default -> throw new UnsupportedCpuArchitectureException("Unsupported architecture");
        };

        case LINUX -> switch (arch) {
            case ARM32, ARM64 -> "natives/linux/Arm";
            case x86_64 -> "natives/linux/X86_64";
            case x86_32 -> "natives/linux/X86_32";
            default -> throw new UnsupportedCpuArchitectureException("Unsupported architecture");
        };

        case MAC -> switch (arch) {
            case ARM32, ARM64 -> "natives/mac/apple-silicon";
            case x86_64, x86_32 -> "natives/mac/intel";
            default -> throw new UnsupportedCpuArchitectureException("Unsupported architecture");
        };

        default -> throw new UnSupportedOSException("Unsupported operating system.");

    };

    private static final String DEV_NATIVES_PATH = switch (os) {
        case WINDOWS -> switch (arch) {
            case ARM64, ARM32 -> "/home/Ahmed/IdeaProjects/java-mediaplayer/natives/windows/Arm";
            case x86_64 -> "/home/Ahmed/IdeaProjects/java-mediaplayer/natives/windows/X86_64";
            case x86_32 -> "/home/Ahmed/IdeaProjects/java-mediaplayer/natives/windows/X86_32";
            default -> throw new UnsupportedCpuArchitectureException("Unsupported architecture");
        };
        case LINUX -> switch (arch) {
            case ARM64, ARM32 -> "/home/Ahmed/IdeaProjects/java-mediaplayer/natives/linux/Arm";
            case x86_64 -> "/home/Ahmed/IdeaProjects/java-mediaplayer/natives/linux/X86_64";
            case x86_32 -> "/home/Ahmed/IdeaProjects/java-mediaplayer/natives/linux/X86_32";
            default -> throw new UnsupportedCpuArchitectureException("Unsupported architecture");
        };
        case MAC -> switch (arch) {
            case ARM64, ARM32 -> "/home/Ahmed/IdeaProjects/java-mediaplayer/mac/apple-silicon";
            case x86_32, x86_64 -> "/home/Ahmed/IdeaProjects/java-mediaplayer/mac/intel";
            default -> throw new UnsupportedCpuArchitectureException("Unsupported architecture");
        };
        default -> throw new UnsupportedCpuArchitectureException("Unsupported operating system.");
    };

    private static String vlcNativesPath = DEFAULT_NATIVES_PATH;

    private VLCConfig() {

    }

    public static void init() {
        try {
            Path jarPath = Path.of(
                    VLCConfig.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );

            Path appDir = Files.isDirectory(jarPath)
                    ? jarPath
                    : jarPath.getParent();
            Path vlcPath;

            if (Boolean.getBoolean("moka.dev")) {
                vlcPath = Path.of(DEV_NATIVES_PATH);
            } else vlcPath = appDir.resolve(vlcNativesPath);

            System.out.println("OS:" + os.name() + "-" + arch.name());
            System.out.println("natives:" + vlcPath);

            if (Files.exists(vlcPath.resolve("plugins"))) {
                System.setProperty("jna.library.path", vlcPath.toString());
                System.setProperty("VLC_PLUGIN_PATH", vlcPath.resolve("plugins").toString());

                logger.info("Using bundled VLC binaries resolved at: {}", vlcPath);
            } else {
                logger.warn("Bundled VLC binaries not found at layout: {}. Falling back to standard system VLC paths.", vlcPath);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}