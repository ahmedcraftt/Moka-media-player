package bootstrap;

import config.VlcConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;

public final class VlcBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(VlcBootstrap.class);

    public static void init() {
        try {
            VlcConfig.init();

            new NativeDiscovery().discover();

            try {
                MediaPlayerFactory factory = new MediaPlayerFactory();
                logger.info("VLC Native Engine loaded from: {}", factory.nativeLibraryPath());
                factory.release();
            } catch (UnsatisfiedLinkError e) {
                throw new RuntimeException("JNA could not link to libvlc.so even after discovery. " +
                        "Ensure vlc-devel is installed and --enable-native-access is set.", e);
            }

            logger.debug("jna.library.path is currently resolved to: {}", System.getProperty("jna.library.path"));
            logger.info("VLC initialized successfully!");

        } catch (Exception e) {
            logger.error("VLC initialization sequence crashed unexpectedly.", e);
        }
    }
}