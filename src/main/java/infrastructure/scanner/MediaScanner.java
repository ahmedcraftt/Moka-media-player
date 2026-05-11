package infrastructure.scanner;

import domain.model.MediaType;
import domain.model.Track;
import infrastructure.classifier.TrackClassifier;
import infrastructure.media.MetaDataManager;
import infrastructure.factory.TrackFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class MediaScanner {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "mp3", "flac", "wav", "m4a", "ogg", "aac", "opus", "wma", "alac"
    );

    private final MetaDataManager metadata;
    private final TrackClassifier classifier = new TrackClassifier();

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
                            Track track = TrackFactory.create(path);

                            metadata.readMetadata(track);

                            MediaType type =
                                    classifier.classify(path, track.getMetadata());

                            track.setType(type);

                            result.add(track);

                        } catch (Exception e) {
                            System.err.println("Skipping: " + path);
                        }
                    });

        } catch (IOException e) {
            throw new RuntimeException(e);
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