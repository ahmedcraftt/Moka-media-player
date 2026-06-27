package domain.model.media;

public record TrackSyncState(
        boolean exists,
        long lastModified,
        long fileSize,
        String mediaType
) {
}
