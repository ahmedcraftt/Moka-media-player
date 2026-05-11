package domain.model;

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

    public Track(String fileName, Path filePath) {
        this.fileName=fileName;
        this.filePath = filePath;
        metadata = new TrackMetadata();
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
        System.out.println("is favorite:" + this.favorite);
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

    public void setTitle(String title) {
        if (title != null) {
            metadata.setTitle(title);
        } else metadata.setTitle(this.fileName);
    }

    public String getTitle() {
        if (metadata.getTitle() != null) {
            return metadata.getTitle();
        } else return fileName;
    }

    @Override
    public String toString() {
        return String.format("%s (%ds)%n + is favorite: %s", this.getTitle(), getMetadata().getDurationInSeconds(), favorite);
    }
}
