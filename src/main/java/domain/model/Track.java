package domain.model;

import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;

public class Track implements AudioSource {

    private final Filedata filedata;
    private final Metadata metadata;
    private final LocalDate dateAdded;
    private MediaType type;
    private MediaMetadata mediaMetadata;
    private boolean favorite = false;
    private int timesPlayed = 0;

    public Track() {
        metadata = new Metadata();
        filedata = new Filedata();
        dateAdded = LocalDate.now();
    }

    public Track(String fileName, Path filePath) {
        metadata = new Metadata();
        filedata = new Filedata();
        filedata.setFileName(fileName);
        filedata.setFilePath(filePath);
        dateAdded = LocalDate.now();
    }

    public Track(String title, boolean favorite, int timesPlayed, MediaType type, Path filepath, LocalDate dateAdded) {
        metadata = new Metadata();
        filedata = new Filedata();
        metadata.setTitle(title);
        filedata.setFilePath(filepath);
        setFavorite(favorite);
        setTimesPlayed(timesPlayed);
        setType(type);
        this.dateAdded = dateAdded;
    }


    @Override
    public boolean isFavorite() {
        return favorite;
    }

    @Override
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
        if (timesPlayed < 0) throw new IllegalArgumentException("TimesPlayed can't be negative");
        this.timesPlayed = timesPlayed;
    }

    public LocalDate getDateAdded() {
        return dateAdded;
    }

    @Override
    public URI getResource() {
        return this.filedata.getFilePath().toUri();
    }

    public synchronized void incrementTimesPlayed() {
        this.timesPlayed++;
    }

    @Override
    public String getTitle() {
        return metadata.getTitle();
    }

    public String getFileName() {
        return filedata.getFileName();
    }

    public MediaMetadata getMediaMetadata() {
        return mediaMetadata;
    }

    public void setMediaMetadata(MediaMetadata mediaMetadata) {
        this.mediaMetadata = mediaMetadata;
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", this.getTitle(), metadata.getDurationInSeconds());
    }

    public String toText() {
        return "Track{" +
                "\nfavorite=" + favorite +
                ", \ntype=" + type +
                ", \nfileData=" + filedata +
                ", \nmetadata=" + metadata +
                ", \ntimesPlayed=" + timesPlayed +
                ", \ndateAdded=" + dateAdded +
                '}';
    }

}
