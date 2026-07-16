package domain.model.media;

import domain.model.metadata.Filedata;
import domain.model.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Track implements AudioSource, Displayable {

    private static final Logger logger = LoggerFactory.getLogger(Track.class);

    private final Filedata filedata;
    private final Metadata metadata;
    private final LocalDate dateAdded;
    private LocalDateTime lastPlayed;
    private MediaType type;
    private int timesPlayed;
    private boolean favorite;

    public Track(Path filePath) {
        metadata = new Metadata();
        filedata = new Filedata();
        filedata.setFileName(filePath.getFileName().toString());
        filedata.setFilePath(filePath);
        dateAdded = LocalDate.now();

        logger.debug("Track constructor(String, Path) called for: {}", this.getTitle());
    }

    public Track(Metadata metadata,
                 int metadataId,
                 boolean favorite,
                 int timesPlayed,
                 MediaType type,
                 LocalDate dateAdded,
                 Path filepath,
                 String filename,
                 LocalDate dateCreated,
                 LocalDate dateModified,
                 LocalDate lastAccessed,
                 String fileType,
                 long fileSize,
                 LocalDateTime lastPlayed
    ) {
        this.metadata = metadata;
        this.metadata.setId(metadataId);
        filedata = new Filedata(
                filepath,
                filename,
                dateCreated,
                dateModified,
                lastAccessed,
                fileType,
                fileSize);
        setFavorite(favorite);
        setTimesPlayed(timesPlayed);
        setType(type);
        setLastPlayed(lastPlayed);
        this.dateAdded = dateAdded;

        logger.debug("Track multi-arg constructor called for: {}", this.getTitle());
        logger.debug("Metadata state: {}", this.metadata.toText());
    }

    @Override
    public boolean isFavorite() {
        return favorite;
    }

    @Override
    public String getDescription() {
        return metadata.getDescription();
    }

    @Override
    public String getArtworkPath() {
        return metadata.getArtworkPath();
    }

    @Override
    public String getGenre() {
        return metadata.getGenre();
    }

    @Override
    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
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

    public String getFileName() {
        return filedata.getFileName();
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

    public LocalDateTime getLastPlayed() {
        return lastPlayed;
    }

    public String getLastPlayedAsString() {
        if (lastPlayed == null) {
            return "none";
        }

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy/MM/dd/HH:mm");

        return lastPlayed.format(formatter);
    }

    public void setLastPlayed(LocalDateTime lastPlayed) {
        if (lastPlayed == null) return;
        this.lastPlayed = lastPlayed;
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