package infrastructure.scanner;

import domain.model.MediaType;
import domain.model.Track;
import infrastructure.classifier.TrackClassifier;
import infrastructure.media.DataResolver;
import infrastructure.media.FiledataManager;
import infrastructure.media.MetadataManager;
import infrastructure.factory.TrackFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class MediaScanner {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "mp3", "flac", "wav", "m4a", "ogg", "aac", "opus", "wma", "alac"
    );

    private final MetadataManager metadata;
    private final FiledataManager filedata;
    private final TrackClassifier classifier = new TrackClassifier();
    private final DataResolver resolver = new DataResolver();

    public MediaScanner(MetadataManager metadata, FiledataManager filedata) {
        this.metadata = metadata;
        this.filedata = filedata;
    }

    public List<Track> scan(Path root) {

        int threads = Math.max(2,
                Runtime.getRuntime().availableProcessors());

        ExecutorService metadataPool =
                Executors.newFixedThreadPool(threads);

        List<Track> result =
                Collections.synchronizedList(new ArrayList<>());

        try {

            discover(root, metadataPool, result);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {

            metadataPool.shutdown();

            try {
                if (!metadataPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    metadataPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                metadataPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        return result;
    }

    private Track processTrack(String path) {

        Track track = TrackFactory.create(path);

        filedata.read(track);

        try {
            metadata.read(track);
        } catch (Exception e) {
            System.err.println("Metadata failed: " + path);
        }

        track.getMetadata().setDurationInSeconds(
                resolver.resolveMissingDuration(track)
        );
        track.getMetadata().setTitle(
                resolver.resolveMissingTitle(track)
        );

        track.setType(
                classifier.classify(path, track.getMetadata())
        );

        return track;
    }

    private void discover(
            Path root,
            ExecutorService metadataPool,
            List<Track> result
    ) throws IOException {

        try (var paths = Files.walk(root)) {

            paths.filter(Files::isRegularFile)
                    .filter(this::isAudioFile)
                    .forEach(path -> {

                        metadataPool.submit(() -> {

                            try {

                                Track track = processTrack(path);

                                if (track != null) {
                                    result.add(track);
                                }

                            } catch (Exception e) {

                                System.err.println(
                                        "Failed processing: " + path
                                );

                                e.printStackTrace();
                            }
                        });
                    });
        }
    }

    private boolean isAudioFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        int dot = name.lastIndexOf('.');
        if (dot == -1) return false;

        String ext = name.substring(dot + 1);
        return SUPPORTED_EXTENSIONS.contains(ext);
    }
}