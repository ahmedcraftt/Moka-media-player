package domain.model;

public record TrackState(
        boolean exists,
        long lastModified,
        long fileSize,
        String mediaType
) {
}
