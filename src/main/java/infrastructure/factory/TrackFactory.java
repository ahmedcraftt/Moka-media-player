package infrastructure.factory;

import domain.model.Track;

import java.nio.file.Path;

public class TrackFactory {

    public static Track create(Path path) {
        return new Track(
                path.getFileName().toString(),
                path
        );
    }
}

