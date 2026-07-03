package infrastructure.scanner;

import domain.model.metadata.Metadata;
import domain.model.media.MediaType;
import domain.model.media.Track;
import domain.model.media.TrackSyncState;
import infrastructure.classifier.TrackClassifier;
import infrastructure.media.DataResolver;
import infrastructure.media.FiledataManager;
import infrastructure.media.MetadataManager;
import infrastructure.storage.TrackStorage;
import infrastructure.storage.MetadataStorage;
import infrastructure.storage.ArtworkStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class MediaScanner {

    private static final Logger logger = LoggerFactory.getLogger(MediaScanner.class);

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "mp3", "flac", "wav", "m4a", "ogg", "aac", "opus", "wma", "alac"
    );

    private final Object dbLock = new Object();

    private final MetadataManager metadataManager;
    private final FiledataManager filedataManager;
    private final TrackStorage trackStorage;
    private final MetadataStorage metadataStorage;
    private final ArtworkStorage artworkStorage;
    private final TrackClassifier classifier = new TrackClassifier();
    private final DataResolver resolver = new DataResolver();

    public MediaScanner(
            MetadataManager metadataManager,
            FiledataManager filedataManager,
            TrackStorage trackStorage,
            MetadataStorage metadataStorage,
            ArtworkStorage artworkStorage
    ) {
        this.metadataManager = metadataManager;
        this.filedataManager = filedataManager;
        this.trackStorage = trackStorage;
        this.metadataStorage = metadataStorage;
        this.artworkStorage = artworkStorage;
    }

    public List<Track> scan(Path root) {
        int threads = Math.max(2, Runtime.getRuntime().availableProcessors());
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        try {
            List<Path> paths = discover(root);
            List<TrackSyncResult> results = processAll(paths, pool);

            persist(results);

            return results.stream()
                    .map(TrackSyncResult::track)
                    .toList();
        } catch (Exception e) {
            logger.error("Media scanner critically failed on root path: {}", root, e);
            throw new MediaScanException(e);
        } finally {
            pool.shutdown();

            try {
                if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
                    logger.warn("Media scanner worker threads did not terminate in time. Forcing shutdown.");
                    pool.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for media scanner worker threads to terminate.", e);
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void persist(List<TrackSyncResult> results) {
        synchronized (dbLock) {
            for (TrackSyncResult result : results) {
                Track track = result.track();
                if (track == null) continue;

                if (result.exists() && !result.metadataChanged()) {
                    continue;
                }

                Metadata md = track.getMetadata();
                if (result.metadataChanged()) {
                    metadataStorage.update(md);
                    logger.debug("Persist updated metadata: {}", md.toText());
                } else {
                    int id = metadataStorage.saveAndGetId(md);
                    md.setId(id);
                }

                trackStorage.save(track);
            }
        }
    }

    private List<TrackSyncResult> processAll(List<Path> paths, ExecutorService pool) {
        List<Future<TrackSyncResult>> futures = new ArrayList<>();

        for (Path path : paths) {
            futures.add(pool.submit(() -> {
                TrackSyncState state = trackStorage.getState(path);
                boolean exists = state.exists();
                boolean fileChanged = false;

                if (exists) {
                    long diskLastModified = Files.getLastModifiedTime(path).toMillis();
                    long diskSize = Files.size(path);

                    long dbModifiedSecs = state.lastModified() / 1000;
                    long diskModifiedSecs = diskLastModified / 1000;

                    fileChanged = (dbModifiedSecs != diskModifiedSecs || state.fileSize() != diskSize);
                }

                Track track = trackStorage.load(path.toString());

                if (track == null) {
                    track = new Track(path.getFileName().toString(), path);
                }

                if (!exists || fileChanged) {
                    processTrack(track, path, state);
                }

                return new TrackSyncResult(track, exists, fileChanged);
            }));
        }

        return futures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        throw new MediaScanException(e);
                    }
                })
                .toList();
    }

    private void processTrack(Track track, Path path, TrackSyncState state) {

        int existingMetadataId = track.getMetadata() != null ? track.getMetadata().getId() : 0;

        logger.debug("Metadata state before engine read: {}", track.getMetadata().toText());
        filedataManager.read(track);
        try {
            metadataManager.read(track);
        } catch (Exception e) {
            logger.warn("Skipping reading unreadable audio: {}", path);
        }
        logger.debug("Metadata state after engine read: {}", track.getMetadata().toText());

        if (existingMetadataId > 0 && track.getMetadata() != null) {
            track.getMetadata().setId(existingMetadataId);
        }

        if (!state.exists()) {
            track.setType(classifier.classify(path, track.getMetadata()));
        } else {
            track.setType(MediaType.StringToMediaType(state.mediaType()));
        }

        byte[] rawArtworkBytes = metadataManager.extractRawArtworkBytes(path);

        logger.debug("Artwork byte length extracted: {}", (rawArtworkBytes == null ? "null" : rawArtworkBytes.length));

        if (rawArtworkBytes != null && rawArtworkBytes.length > 0) {
            String artworkHash = UUID.nameUUIDFromBytes(rawArtworkBytes).toString();
            String artworkFileName = artworkHash + ".jpg";
            String diskPath;

            logger.debug("Processing artwork injection -> Title: '{}', Hash: {}, Existing Path: {}",
                    track.getMetadata().getTitle(), artworkHash, track.getMetadata().getArtworkPath());

            try {
                diskPath = artworkStorage.saveArtwork(rawArtworkBytes, artworkFileName);
            } catch (IOException e) {
                logger.error("Failed to write artwork file to disk for track: {}", path, e);
                throw new MediaScanException("failed to save artwork " + path);
            }

            track.getMetadata().setArtworkPath(diskPath);
        }

        Metadata metadata = track.getMetadata();
        if (metadata != null) {
            metadata.setDurationInSeconds(resolver.resolveMissingDuration(track));
            metadata.setTitle(resolver.resolveMissingTitle(track));
        }

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

    private record TrackSyncResult(Track track, boolean exists, boolean metadataChanged) {
    }
}