package config;

import platform.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class VLCConfig {

    private static final Logger logger = LoggerFactory.getLogger(VLCConfig.class);

    private static final OS os = OSDetector.getOS();
    private static final CpuArch arch = ArchDetector.getArch();

    private static final String DEFAULT_NATIVES_PATH = switch (os) {
        case WINDOWS -> switch (arch) {
            case ARM32, ARM64 -> "natives/windows/Arm";
            case x86_64 -> "natives/windows/X86_64";
            case x86_32 -> "natives/windows/X86_32";
            case null, default -> throw new UnsupportedCpuArchitectureException("Unsupported architecture");
        };

        case LINUX -> switch (arch) {
            case ARM32, ARM64 -> "natives/linux/Arm";
            case x86_64 -> "natives/linux/X86_64";
            case x86_32 -> "natives/linux/X86_32";
            case null, default -> throw new UnsupportedCpuArchitectureException("Unsupported architecture");
        };

        case MAC -> switch (arch) {
            case ARM32, ARM64 -> "natives/mac/apple-silicon";
            case x86_64, x86_32 -> "natives/mac/intel";
            case null, default -> throw new UnsupportedCpuArchitectureException("Unsupported architecture");
        };

        case null, default -> throw new UnSupportedOSException("Unsupported operating system.");
    };

    private static final String DEV_NATIVES_PATH = "/home/Ahmed/IdeaProjects/java-mediaplayer/natives/linux/X86_64";

    private VLCConfig() {
    }

    public static void init() {
        Path vlcPath;

        if (Boolean.getBoolean("moka.dev")) {
            vlcPath = Path.of(DEV_NATIVES_PATH);
        } else {
            vlcPath = locateNativesDirectory();
        }

        if (vlcPath != null && Files.exists(vlcPath)) {
            String absPath = vlcPath.toAbsolutePath().toString();
            System.setProperty("jna.library.path", absPath);

            Path pluginsPath = vlcPath.resolve("plugins");
            if (Files.exists(pluginsPath)) {
                System.setProperty("VLC_PLUGIN_PATH", pluginsPath.toAbsolutePath().toString());
            }

            logger.info("Successfully bound native VLC binaries at: {}", absPath);
        } else {
            logger.warn("Bundled VLC binaries not found in application directories. Falling back to system discovery.");
        }
    }

    private static Path locateNativesDirectory() {
        List<Path> baseSearchDirs = new ArrayList<>();

        ProcessHandle.current().info().command().ifPresent(cmd -> {
            try {
                Path execPath = Path.of(cmd).toAbsolutePath();
                Path binDir = execPath.getParent();
                if (binDir != null) {
                    baseSearchDirs.add(binDir);
                    if (binDir.getParent() != null) {
                        baseSearchDirs.add(binDir.getParent());
                    }
                }
            } catch (Exception e) {
                logger.debug("Failed to parse process command path", e);
            }
        });

        try {
            Path procExe = Path.of("/proc/self/exe");
            if (Files.exists(procExe)) {
                Path realExecPath = procExe.toRealPath();
                Path binDir = realExecPath.getParent();
                if (binDir != null) {
                    baseSearchDirs.add(binDir);
                    if (binDir.getParent() != null) {
                        baseSearchDirs.add(binDir.getParent());
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to read /proc/self/exe link", e);
        }

        String javaHome = System.getProperty("java.home");
        if (javaHome != null) {
            Path jhPath = Path.of(javaHome).toAbsolutePath();
            baseSearchDirs.add(jhPath);
            if (jhPath.getParent() != null) baseSearchDirs.add(jhPath.getParent());
        }

        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        baseSearchDirs.add(cwd);
        if (cwd.getParent() != null) baseSearchDirs.add(cwd.getParent());

        for (Path root : baseSearchDirs) {
            List<Path> targetCandidates = List.of(
                    root.resolve("lib/app/natives"),
                    root.resolve("lib/app").resolve(DEFAULT_NATIVES_PATH),
                    root.resolve("natives"),
                    root.resolve(DEFAULT_NATIVES_PATH)
            );

            for (Path candidate : targetCandidates) {
                if (containsVlcLibraries(candidate)) {
                    return candidate;
                }
            }
        }

        return null;
    }

    private static boolean containsVlcLibraries(Path dir) {
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            return false;
        }

        return Files.exists(dir.resolve("libvlc.so"))
                || Files.exists(dir.resolve("libvlc.dll"))
                || Files.exists(dir.resolve("libvlc.dylib"))
                || Files.exists(dir.resolve("plugins"));
    }
}