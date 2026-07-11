package gui.utils;

import javafx.scene.image.Image;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class ArtworkCache {

    private static final Image DEFAULT_ARTWORK = new Image(
            Objects.requireNonNull(
                    ArtworkCache.class.getResourceAsStream(
                            "/assets/images/unknown.jpg")));

    private static final Map<String, Image> CACHE =
            new ConcurrentHashMap<>();

    private ArtworkCache() {
    }

    public static Image get(String artworkPath) {

        if (artworkPath == null || artworkPath.isBlank()) {
            return DEFAULT_ARTWORK;
        }

        File file = new File(artworkPath);

        if (!file.exists()) {
            return DEFAULT_ARTWORK;
        }

        return CACHE.computeIfAbsent(
                artworkPath,
                path -> new Image(
                        file.toURI().toString(),
                        40,
                        40,
                        true,
                        true,
                        true
                )
        );
    }
}
