package infrastructure.scanner;

import domain.model.metadata.Metadata;
import domain.model.media.MediaType;
import domain.model.media.Track;
import domain.model.media.TrackSyncState;
import infrastructure.classifier.TrackClassifier;
import infrastructure.media.DataResolver;
import infrastructure.media.FiledataManager;
import infrastructure.media.LyricsEmbedder;
import infrastructure.media.MetadataManager;
import infrastructure.storage.DatabaseManager;
import infrastructure.storage.TrackStorage;
import infrastructure.storage.MetadataStorage;
import infrastructure.storage.ArtworkStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

public class DesktopMediaScanner implements MediaScanner {

    private static final Logger logger = LoggerFactory.getLogger(DesktopMediaScanner.class);

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "mp3", "flac", "wav", "m4a", "ogg", "aac", "opus", "wma", "alac"
    );

    private final MetadataManager metadataManager;
    private final FiledataManager filedataManager;
    private final TrackStorage trackStorage;
    private final MetadataStorage metadataStorage;
    private final ArtworkStorage artworkStorage;
    private final LyricsEmbedder lyricsEmbedder;
    private final DataResolver resolver = new DataResolver();

    public DesktopMediaScanner(
            MetadataManager metadataManager,
            FiledataManager filedataManager,
            TrackStorage trackStorage,
            MetadataStorage metadataStorage,
            ArtworkStorage artworkStorage,
            LyricsEmbedder lyricsEmbedder
    ) {
        this.metadataManager = metadataManager;
        this.filedataManager = filedataManager;
        this.trackStorage = trackStorage;
        this.metadataStorage = metadataStorage;
        this.artworkStorage = artworkStorage;
        this.lyricsEmbedder = lyricsEmbedder;
    }

    public Track scan(File file) {
        Path path = file.toPath();
        if (!isAudioFile(path)) throw new MediaScanException("Not an audio file");
        Track track = new Track(path);
        metadataManager.read(track);
        filedataManager.read(track);
        scanForArtwork(track);
        track.setType(TrackClassifier.classify(path, track.getMetadata()));
        try (Connection connection = DatabaseManager.connect()) {
            trackStorage.save(track, connection);
        } catch (SQLException e) {
            logger.error("Error loading track from path: {}", path, e);
        }

        return track;
    }

    public List<Track> scan(List<Path> paths) {
        long start = System.currentTimeMillis();
        if (paths == null || paths.isEmpty()) {
            return Collections.emptyList();
        }

        int threads = Math.max(2, Runtime.getRuntime().availableProcessors());
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        try {
            List<TrackSyncResult> results = processAll(paths, pool);
            long time = System.currentTimeMillis();
            persist(results);
            return results.stream()
                    .map(TrackSyncResult::track)
                    .toList();
        } catch (Exception e) {
            logger.error("Media scanner critically failed while processing explicit paths list", e);
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

    public List<Track> scan(Path root) {
        try {
            List<Path> paths = discover(root);
            return scan(paths);
        } catch (IOException e) {
            logger.error("Media scanner critically failed during path discovery on root: {}", root);
            throw new MediaScanException(e);
        }
    }

    public void scanForArtwork(Track track) {
        Path path = track.getFilePath();

        byte[] rawArtworkBytes = metadataManager.extractRawArtworkBytes(path);

        logger.debug("Artwork byte length extracted: {}", (rawArtworkBytes == null ? "null" : rawArtworkBytes.length));

        if (rawArtworkBytes != null && rawArtworkBytes.length > 0) {
            String artworkHash = UUID.nameUUIDFromBytes(rawArtworkBytes).toString();
            String artworkFileName = artworkHash + ".jpg";
            String diskPath;

            try {
                diskPath = artworkStorage.saveArtwork(rawArtworkBytes, artworkFileName);
            } catch (IOException e) {
                logger.error("Failed to write artwork file to disk for track: {}", path, e);
                throw new MediaScanException("failed to save artwork " + path);
            }

            track.getMetadata().setArtworkPath(diskPath);
        }
    }

    public boolean isAudioFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        int dot = name.lastIndexOf('.');
        if (dot == -1) return false;

        String ext = name.substring(dot + 1);
        return SUPPORTED_EXTENSIONS.contains(ext);
    }

    private void persist(List<TrackSyncResult> results) {
        try (Connection connection = DatabaseManager.connect()) {
            connection.setAutoCommit(false);
            for (TrackSyncResult result : results) {
                Track track = result.track();
                if (track == null) continue;

                if (result.exists() && !result.metadataChanged()) {
                    continue;
                }

                Metadata md = track.getMetadata();
                if (result.metadataChanged()) {
                    metadataStorage.update(md, connection);
                } else {
                    int id = metadataStorage.saveAndGetId(md, connection);
                    md.setId(id);
                }
                trackStorage.save(track, connection);
            }
            connection.commit();
        } catch (SQLException e) {
            logger.error("Failed to create connection to database.", e);
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
                    track = new Track(path);
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
        readTrack(track);
        handleMetadataId(track);
        handleTrackType(track, path, state);
        scanForArtwork(track);
        resolveMissingFields(track);
    }

    private void handleTrackType(Track track, Path path, TrackSyncState state) {
        if (!state.exists()) {
            track.setType(TrackClassifier.classify(path, track.getMetadata()));
        } else {
            track.setType(MediaType.StringToMediaType(state.mediaType()));
        }
    }

    private void readTrack(Track track) {
        lyricsEmbedder.embedLyrics(track);
        filedataManager.read(track);
        metadataManager.read(track);
    }

    private void handleMetadataId(Track track) {
        int existingMetadataId = track.getMetadata() != null ? track.getMetadata().getId() : 0;

        if (existingMetadataId > 0 && track.getMetadata() != null) {
            track.getMetadata().setId(existingMetadataId);
        }
    }

    private void resolveMissingFields(Track track) {
        Metadata metadata = track.getMetadata();
        metadata.setDurationInSeconds(resolver.resolveMissingDuration(track));
        metadata.setTitle(resolver.resolveMissingTitle(track));
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

    private record TrackSyncResult(Track track, boolean exists, boolean metadataChanged) {

    }
}