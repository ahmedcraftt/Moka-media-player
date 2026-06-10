package infrastructure.factory;

import domain.model.MediaType;
import domain.model.Track;

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
            String title,
            boolean favorite,
            int timesPlayed,
            MediaType type,
            Path path,
            LocalDate dateAdded
    ) {
        return new Track(title, favorite, timesPlayed, type, path, dateAdded);
    }

}

