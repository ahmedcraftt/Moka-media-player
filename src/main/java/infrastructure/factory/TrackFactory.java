package infrastructure.factory;

import domain.model.Track;

import java.nio.file.Path;

public final class TrackFactory {

    private TrackFactory() {
    }

    public static Track create(String path) {

        return new Track(
                path.getFileName().toString(),
                path
        );
    }

}

