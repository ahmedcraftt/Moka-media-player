package infrastructure.factory;

import domain.model.metadata.AudioBookData;
import domain.model.metadata.MediaMetadata;
import domain.model.metadata.PodcastData;
import domain.model.metadata.SongData;
import domain.model.media.MediaType;

public final class MediaMetadataFactory {
    private MediaMetadataFactory() {
    }

    public static MediaMetadata create(MediaType mediaType) {
        return switch (mediaType) {
            case AUDIOBOOK -> new AudioBookData();
            case PODCAST -> new PodcastData();
            case SONG -> new SongData();
            default -> throw new IllegalStateException("Unexpected value: " + mediaType);
        };
    }
}
