package domain.model.media;

import domain.model.metadata.Metadata;
import domain.model.metadata.VideoMetadata;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Video extends Track {
    private final VideoMetadata videoMetadata;

    public Video(Metadata metadata, int metadataId, boolean favorite, int timesPlayed,
                 MediaType type, LocalDate dateAdded, Path filepath, String filename,
                 LocalDate dateCreated, LocalDate dateModified, LocalDate lastAccessed,
                 String fileType, long fileSize, LocalDateTime lastPlayed, int height,
                 int width, String resolution, int frameRate) {
        super(metadata, metadataId, favorite, timesPlayed, type, dateAdded,
                filepath, filename, dateCreated, dateModified, lastAccessed,
                fileType, fileSize, lastPlayed);
        videoMetadata = new VideoMetadata();
        this.videoMetadata.setHeight(height);
        this.videoMetadata.setWidth(width);
        this.videoMetadata.setResolution(resolution);
        this.videoMetadata.setFrameRate(frameRate);
    }

    public Video(Path path) {
        super(path);
        videoMetadata = new VideoMetadata();
    }

    public VideoMetadata getVideoMetadata() {
        return videoMetadata;
    }

}
