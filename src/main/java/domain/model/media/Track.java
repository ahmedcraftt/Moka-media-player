package domain.model.media;

import domain.model.metadata.Filedata;
import domain.model.metadata.MediaMetadata;
import domain.model.metadata.Metadata;

import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Objects;

public class Track implements AudioSource, Displayable {

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
        IO.println(this.favorite);
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

    public Path getFilePath() {
        return filedata.getFilePath();
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

    public void incrementTimesPlayed() {
        this.timesPlayed++;
    }

    @Override
    public String getTitle() {
        return metadata.getTitle();
    }

    @Override
    public byte[] getArtwork() {
        return metadata.getArtwork();
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
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Track track)) return false;
        return Objects.equals(getFilePath(), track.getFilePath());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getFilePath());
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
