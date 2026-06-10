package domain.model;

public record TrackSyncState(
        boolean exists,
        long lastModified,
        long fileSize,
        String mediaType
) {
}
