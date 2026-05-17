package infrastructure.scanner;

import domain.model.TrackState;
import domain.model.MediaType;
import domain.model.Track;
import domain.model.TrackTask;
import infrastructure.classifier.TrackClassifier;
import infrastructure.media.DataResolver;
import infrastructure.media.FiledataManager;
import infrastructure.media.MetadataManager;
import infrastructure.factory.TrackFactory;
import infrastructure.storage.TrackStorage;

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
    private final TrackStorage storage;
    private final TrackClassifier classifier = new TrackClassifier();
    private final DataResolver resolver = new DataResolver();

    public MediaScanner(MetadataManager metadata, FiledataManager filedata, TrackStorage storage) {
        this.metadata = metadata;
        this.filedata = filedata;
        this.storage = storage;
    }

    public List<Track> scan(Path root) {

        int threads = Math.max(2,
                Runtime.getRuntime().availableProcessors());

        ExecutorService pool =
                Executors.newFixedThreadPool(threads);

        try {

            List<Path> paths = discover(root);

            List<TrackTask> tasks = processAll(paths, pool);

            persist(tasks);

            return tasks.stream()
                    .map(TrackTask::track)
                    .toList();

        } catch (Exception e) {
            throw new RuntimeException(e);

        } finally {
            pool.shutdown();
        }
    }

    private void persist(List<TrackTask> tasks) {

        for (TrackTask task : tasks) {

            Path path = task.path();
            Track track = task.track();

            try {

                TrackState state = storage.getState(path);

                boolean exists = state.exists();

                boolean needsUpdate = exists &&
                        (
                                state.lastModified() != Files.getLastModifiedTime(path).toMillis()
                                        || state.fileSize() != Files.size(path)
                        );

                if (exists && !needsUpdate) {
                    continue;
                }

                if (needsUpdate) {
                    storage.update(track);
                } else {
                    storage.save(track);
                }

            } catch (Exception e) {
                System.err.println("DB sync failed: " + path);
                e.printStackTrace();
            }
        }
    }

    private List<TrackTask> processAll(List<Path> paths, ExecutorService pool) {

        List<Future<TrackTask>> futures = new ArrayList<>();

        for (Path path : paths) {

            futures.add(pool.submit(() -> {
                TrackState state = storage.getState(path);
                Track track = processTrack(path, state);
                return new TrackTask(path, track);
            }));
        }

        List<TrackTask> result = new ArrayList<>();

        for (Future<TrackTask> f : futures) {
            try {
                result.add(f.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private Track processTrack(Path path, TrackState state) {

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

        if (!state.exists()) {

            track.setType(
                    classifier.classify(path, track.getMetadata())
            );

        } else {

            track.setType(
                    MediaType.StringToMediaType(state.mediaType())
            );
        }

        return track;
    }

    private List<Path> discover(Path root) throws IOException {

        try (var paths = Files.walk(root)) {

            return paths
                    .filter(Files::isRegularFile)
                    .filter(this::isAudioFile)
                    .map(Path::toAbsolutePath)
                    .map(Path::normalize)
                    .toList();
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