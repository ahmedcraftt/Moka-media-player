package infrastructure.scanner;

import domain.model.metadata.Metadata;
import domain.model.media.MediaType;
import domain.model.media.Track;
import domain.model.media.TrackSyncState;
import infrastructure.classifier.TrackClassifier;
import infrastructure.factory.TrackFactory;
import infrastructure.media.DataResolver;
import infrastructure.media.FiledataManager;
import infrastructure.media.MetadataManager;
import infrastructure.storage.TrackStorage;
import infrastructure.storage.MetadataStorage;
import infrastructure.storage.ArtworkStorage;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class MediaScanner {

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
            List<ScanResult> results = processAll(paths, pool);

            persist(results);

            return results.stream()
                    .map(ScanResult::track)
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
            throw new MediaScanException(e);
        } finally {
            pool.shutdown();
        }
    }

    private void persist(List<ScanResult> results) {
        synchronized (dbLock) {
            for (ScanResult result : results) {
                Track track = result.track();
                if (track == null) continue;

                if (result.exists() && !result.needsUpdate()) {
                    continue;
                }

                Metadata md = track.getMetadata();
                if (result.needsUpdate()) {
                    metadataStorage.update(md);
                    IO.println("MediaScanner.java persist() line83:" + md.toText());

                } else {
                    int id = metadataStorage.saveAndGetId(md);
                    md.setId(id);
                }

                trackStorage.save(track);
            }
        }
    }

    private List<ScanResult> processAll(List<Path> paths, ExecutorService pool) {
        List<Future<ScanResult>> futures = new ArrayList<>();

        for (Path path : paths) {
            futures.add(pool.submit(() -> {
                TrackSyncState state = trackStorage.getState(path);
                boolean exists = state.exists();
                boolean needsUpdate = false;

                if (exists) {
                    long diskLastModified = Files.getLastModifiedTime(path).toMillis();
                    long diskSize = Files.size(path);

                    long dbModifiedSecs = state.lastModified() / 1000;
                    long diskModifiedSecs = diskLastModified / 1000;

                    needsUpdate = (dbModifiedSecs != diskModifiedSecs || state.fileSize() != diskSize);
                }

                Track track;
                if (exists && !needsUpdate) {
                    track = trackStorage.load(path.toString());
                    IO.println("MediaScanner.java processAll() line 119:" + track.getMetadata().toText());
                } else {
                    track = processTrack(path, state);
                }

                return new ScanResult(track, exists, needsUpdate);
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

    private Track processTrack(Path path, TrackSyncState state) {
        Track track = trackStorage.load(path.toString());
        if (track == null) {
            track = TrackFactory.create(path);
        }

        int existingMetadataId = track.getMetadata() != null ? track.getMetadata().getId() : 0;

        IO.println("MediaScanner.java processTrack() line144:" + track.getMetadata().toText());
        filedataManager.read(track);
        metadataManager.read(track);
        IO.println("MediaScanner.java processTrack() line147:" + track.getMetadata().toText());

        if (existingMetadataId > 0 && track.getMetadata() != null) {
            track.getMetadata().setId(existingMetadataId);
        }

        if (!state.exists()) {
            track.setType(classifier.classify(path, track.getMetadata()));
        } else {
            track.setType(MediaType.StringToMediaType(state.mediaType()));
        }

        byte[] rawArtworkBytes = metadataManager.extractRawArtworkBytes(path);

        if (rawArtworkBytes != null && rawArtworkBytes.length > 0) {
            String artworkHash = UUID.nameUUIDFromBytes(rawArtworkBytes).toString();
            String artworkFileName = artworkHash + ".jpg";

            String diskPath;
            try {
                diskPath = artworkStorage.saveArtwork(rawArtworkBytes, artworkFileName);
            } catch (IOException e) {
                throw new MediaScanException("failed to save artwork " + path);
            }

            track.getMetadata().setArtworkPath(diskPath);
        }

        Metadata metadata = track.getMetadata();
        if (metadata != null) {
            metadata.setDurationInSeconds(resolver.resolveMissingDuration(track));
            metadata.setTitle(resolver.resolveMissingTitle(track));
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

    private record ScanResult(Track track, boolean exists, boolean needsUpdate) {
    }
}