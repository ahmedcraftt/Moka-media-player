package platform;

public class ArchDetector {

    public static CpuArch getArch() {
        String arch = System.getProperty("os.arch").toLowerCase();

        if (arch.contains("amd64") || arch.contains("x86_64")) {
            return CpuArch.x86_64;
        }
        if (arch.contains("86")) {
            return CpuArch.x86_32;
        }
        if (arch.contains("aarch64")) {
            return CpuArch.ARM64;
        }
        if (arch.contains("arm")) {
            return CpuArch.ARM32;
        }

        IO.println(arch);
        return CpuArch.UNKNOWN;
    }

}
