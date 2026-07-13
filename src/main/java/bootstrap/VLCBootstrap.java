package bootstrap;

import config.VLCConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;

import java.util.concurrent.atomic.AtomicBoolean;

public final class VLCBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(VLCBootstrap.class);

    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    public static void init() {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }

        try {
            VLCConfig.init();

            if (System.getProperty("jna.library.path") == null) {
                logger.info("Bundled paths not found. Running system native discovery...");
                new NativeDiscovery().discover();
            }

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