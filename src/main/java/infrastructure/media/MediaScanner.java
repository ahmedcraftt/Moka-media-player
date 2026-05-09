package infrastructure.media;

import entities.Track;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class MediaScanner {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "mp3", "flac", "wav", "m4a", "ogg", "aac", "opus", "wma", "alac"
    );

    private final MetaDataManager metadata;

    public MediaScanner(MetaDataManager metadata) {
        this.metadata = metadata;
    }

    public List<Track> scan(Path root) {

        List<Track> result = new ArrayList<>();

        try (var paths = Files.walk(root)) {

            paths.filter(Files::isRegularFile)
                    .filter(this::isAudioFile)
                    .forEach(path -> {

                        try {

                            Track track = TrackFactory.createTrack(path, metadata);

                            track.setFilePath(path);

                            metadata.readMetadata(track);

                            result.add(track);

                        } catch (Exception e) {

                            System.err.println(
                                    "Skipping corrupted file: " + path
                            );
                        }
                    });

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed scanning directory: " + root, e
            );
        }

        return result;
    }

    private boolean isAudioFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        int dot = name.lastIndexOf('.');
        if (dot == -1) return false;

        String ext = name.substring(dot + 1);
        return SUPPORTED_EXTENSIONS.contains(ext);
    }
}