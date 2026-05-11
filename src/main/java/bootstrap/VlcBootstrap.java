package bootstrap;

import config.VlcConfig;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;

public class VlcBootstrap {

    public static void init() {
        try {

            VlcConfig.init();

            new NativeDiscovery().discover();

            try {
                MediaPlayerFactory factory = new MediaPlayerFactory();
                System.out.println("✅ VLC Native Engine loaded from: " + factory.nativeLibraryPath());
                factory.release(); // Clean up the temporary test factory
            } catch (UnsatisfiedLinkError e) {
                throw new RuntimeException("JNA could not link to libvlc.so even after discovery. " +
                        "Ensure vlc-devel is installed and --enable-native-access is set.");
            }

            System.out.println("DEBUG: jna.library.path is: " + System.getProperty("jna.library.path"));
            System.out.println("🚀 VLC initialized successfully!");

        } catch (Exception e) {
            System.err.println("❌ VLC initialization failed!");
            e.printStackTrace();
        }
    }

}