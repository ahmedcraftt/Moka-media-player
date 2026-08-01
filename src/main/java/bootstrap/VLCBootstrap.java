package bootstrap;

import config.VLCConfig;
import gui.utils.DialogLauncher;

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
        } catch (Exception e) {
            logger.error("VLCConfig initialization encountered an error. Attempting system discovery fallback...", e);
        }

        logger.info("jna.library.path = {}", System.getProperty("jna.library.path"));

        if (System.getProperty("jna.library.path") == null) {
            logger.info("Bundled paths not found. Running system native discovery...");
            boolean found = new NativeDiscovery().discover();
            logger.info("System native VLC discovery success: {}", found);
        }

        try {
            MediaPlayerFactory factory = new MediaPlayerFactory();
            logger.info("VLC Native Engine loaded from: {}", factory.nativeLibraryPath());
            factory.release();
            logger.info("VLC initialized successfully!");
        } catch (Throwable t) {
            logger.error("Failed to initialize VLC native libraries", t);
            DialogLauncher.showVlcMissingDialog(t);
            throw new RuntimeException("VLC native libraries could not be loaded.", t);
        }
    }

}