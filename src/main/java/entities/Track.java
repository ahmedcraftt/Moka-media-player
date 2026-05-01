package entities;

import java.nio.file.Path;
import java.time.LocalDate;

public class Track {

    private MediaType type;
    private LocalDate dateCreated;
    private LocalDate dateModified;
    private long fileSize;
    private Path filePath;
    private String fileName;
    private boolean favorite;
    private TrackMetadata metadata;

    public Track() {}

    public Track(String fileName, TrackMetadata metadata, Path filePath,MediaType type) {
        this.type=type;
        this.fileName=fileName;
        this.metadata=metadata;
        this.filePath = filePath;
        favorite = false;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LocalDate getDateModified() {
        return dateModified;
    }

    public void setDateModified(LocalDate dateModified) {
        this.dateModified = dateModified;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public Path getFilePath() {
        return filePath;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public MediaType getType() {
        return type;
    }

    public void setType(MediaType type) {
        this.type = type;
    }

    public TrackMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(TrackMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return String.format("%s (%ds)%n", getMetadata().getTitle(), getMetadata().getDurationInSeconds());
    }
}
