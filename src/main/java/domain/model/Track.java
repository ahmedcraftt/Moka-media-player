package domain.model;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

public class Track {

    private MediaType type;
    private Filedata fileData;
    private Metadata metadata;
    private boolean favorite = false;
    private boolean playing = false;
    private final AtomicInteger timesPlayed = new AtomicInteger(0);

    public Track() {}

    public Track(String fileName, String filePath) {
        metadata = new Metadata();
        fileData = new Filedata(filePath, fileName);
        type = MediaType.SONG;
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
        if (type != null)
            this.type = type;
        else throw new IllegalArgumentException("Media type is null");
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        if (metadata != null) {
            this.metadata = metadata;
        } else throw new RuntimeException("metadata is null");
    }

    public Filedata getFileData() {
        return fileData;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public AtomicInteger getTimesPlayed() {
        return timesPlayed;
    }

    public void setTimesPlayed(int timesPlayed) {
        this.timesPlayed.set(timesPlayed);
    }

    public void incrementTimesPlayed() {
        this.timesPlayed.getAndIncrement();
    }

    public void setTitle(String title) {
        if (title != null && !title.isBlank() && !title.equalsIgnoreCase("unknown")) {
            metadata.setTitle(title);
        } else if (fileData.getFileName() != null) {
            metadata.setTitle(removeExtension(fileData.getFileName()));
        } else throw new IllegalArgumentException("title");
    }

    public String getTitle() {
        if (metadata.getTitle() != null
                && !metadata.getTitle().isBlank()
                && !metadata.getTitle().equalsIgnoreCase("unknown")) {
            return metadata.getTitle();
        } else return removeExtension(fileData.getFileName());
    }

    public String getFilePath() {
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
        return String.format("%s (%s)", this.getTitle(), this.getDuration());
    }

    private String removeExtension(String fileName) {
        if (fileName == null) return null;

        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) return fileName;

        return fileName.substring(0, lastDot);
    }

    private String formatTime(long seconds) {
        long mins = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }
}
