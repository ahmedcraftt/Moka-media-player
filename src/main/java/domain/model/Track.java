package domain.model;

import java.nio.file.Path;

public class Track {

    private MediaType type;
    private Filedata fileData;
    private Metadata metadata;
    private boolean favorite;

    public Track() {}

    public Track(String fileName, Path filePath) {
        metadata = new Metadata();
        fileData = new Filedata(filePath, fileName);
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
        System.out.println("is favorite:" + this.favorite);
    }

    public MediaType getType() {
        return type;
    }

    public void setType(MediaType type) {
        this.type = type;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public Filedata getFileData() {
        return fileData;
    }

    public void setTitle(String title) {
        if (title != null) {
            metadata.setTitle(title);
        } else metadata.setTitle(removeExtension(fileData.getFileName()));
    }

    public String getTitle() {
        if (metadata.getTitle() != null) {
            return metadata.getTitle();
        } else return removeExtension(this.getFileName());
    }

    public Path getFilePath() {
        return fileData.getFilePath();
    }

    public String getFileName() {
        return fileData.getFileName();
    }

    public String getDuration() {
        return formatTime(metadata.getDurationInSeconds());
    }

    @Override
    public String toString() {
        return String.format("%s (%s)%n + is favorite: %s", this.getTitle(), this.getDuration(), favorite);
    }

    private String removeExtension(String fileName) {
        if (fileName == null) return null;

        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) return fileName; // no extension

        return fileName.substring(0, lastDot);
    }

    private String formatTime(long seconds) {
        long mins = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }
}
