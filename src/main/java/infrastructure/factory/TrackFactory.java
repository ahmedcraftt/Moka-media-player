package infrastructure.factory;

import domain.model.media.MediaType;
import domain.model.media.Track;
import domain.model.metadata.Metadata;

import java.nio.file.Path;
import java.time.LocalDate;

public final class TrackFactory {

    private TrackFactory() {
    }

    public static Track create(Path path) {

        return new Track(
                path.getFileName().toString(),
                path
        );
    }

    public static Track create(
            Metadata metadata,
            int mediaId,
            boolean favorite,
            int timesPlayed,
            MediaType type,
            Path path,
            LocalDate dateAdded
    ) {
        return new Track(metadata, mediaId, favorite, timesPlayed, type, path, dateAdded);
    }

}

