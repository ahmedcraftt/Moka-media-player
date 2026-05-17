package domain.model;

import java.nio.file.Path;

public class Track {

    private MediaType type;
    private final Filedata filedata;
    private final Metadata metadata;
    private boolean favorite = false;
    private int timesPlayed = 0;

    public Track() {
        metadata = new Metadata();
        filedata = new Filedata();
    }

    public Track(String fileName, Path filePath) {
        metadata = new Metadata();
        filedata = new Filedata(filePath, fileName);
    }

    public Track(String title, boolean favorite, int timesPlayed, MediaType type, Path filepath) {
        metadata = new Metadata();
        filedata = new Filedata();
        metadata.setTitle(title);
        filedata.setFilePath(filepath);
        this.favorite = favorite;
        this.timesPlayed = timesPlayed;
        this.type = type;
    }


    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
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

    public Filedata getFiledata() {
        return filedata;
    }

    public int getTimesPlayed() {
        return timesPlayed;
    }

    public void setTimesPlayed(int timesPlayed) {
        this.timesPlayed = timesPlayed;
    }

    public void incrementTimesPlayed() {
        this.timesPlayed++;
    }

    public void setTitle(String title) {
        if (title != null && !title.isBlank() && !title.equalsIgnoreCase("unknown")) {
            metadata.setTitle(title);
        } else if (filedata.getFileName() != null) {
            metadata.setTitle(removeExtension(filedata.getFileName()));
        } else throw new IllegalArgumentException("title");
    }

    public String getTitle() {
        if (metadata.getTitle() != null
                && !metadata.getTitle().isBlank()
                && !metadata.getTitle().equalsIgnoreCase("unknown")) {
            return metadata.getTitle();
        } else return removeExtension(filedata.getFileName());
    }

    public String getDuration() {
        return formatTime(metadata.getDurationInSeconds());
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", this.getTitle(), this.getDuration());
    }

    public String toText() {
        return "Track{" +
                "\nfavorite=" + favorite +
                ", \ntype=" + type +
                ", \nfileData=" + filedata +
                ", \nmetadata=" + metadata +
                ", \ntimesPlayed=" + timesPlayed +
                '}';
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
