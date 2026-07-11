package domain.model.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;

public class Metadata {

    private static final Logger logger = LoggerFactory.getLogger(Metadata.class);

    private int id;
    private int trackNumber = 0;
    private int durationInSeconds = 0;
    private long bitrate = 0;
    private long samplerate = 0;
    private String title = "Unknown";
    private String genre = "Unknown";
    private String description = "";
    private String lyrics = "";
    private String series = "";
    private String artist = "Unknown";
    private String seriesArtist = "";
    private String artworkPath = "Unknown";
    private Language language;
    private Year year = Year.of(0);

    public Metadata() {
    }

    public Metadata(
            int id,
            int durationInSeconds,
            long bitrate,
            long samplerate,
            String title,
            String genre,
            String description,
            String lyrics,
            String language,
            Year year,
            String artworkPath,
            String series,
            String artist,
            String seriesArtist,
            int trackNumber
    ) {
        setId(id);
        setDurationInSeconds(durationInSeconds);
        setBitrate(bitrate);
        setSamplerate(samplerate);
        setTitle(title);
        setGenre(genre);
        setDescription(description);
        setLyrics(lyrics);
        setLanguage(language);
        setYear(year);
        setArtworkPath(artworkPath);
        setSeries(series);
        setArtist(artist);
        setSeriesArtist(seriesArtist);
        setTrackNumber(trackNumber);

        logger.debug("Metadata initialized: {}", this.toText());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLyrics(String lyrics) {
        if (lyrics != null && !lyrics.isBlank()) {
            this.lyrics = lyrics;
        } else this.lyrics = "";
    }

    public String getLyrics() {
        return lyrics;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description != null && !description.isBlank()) {
            this.description = description;
        } else this.description = "";
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        if (genre != null && !genre.isBlank()) {
            this.genre = genre;
        } else this.genre = "Unknown";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        } else this.title = "Unknown";
    }

    public int getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(int durationInSeconds) {
        this.durationInSeconds = Math.max(durationInSeconds, 0);
    }

    public void setLanguage(String language) {
        if (language != null && !language.isBlank()) {
            this.language = new Language(language);
        } else throw new IllegalArgumentException("Language cannot be null or blank");
    }

    public Language getLanguage() {
        return language;
    }

    public void setYear(Year year) {
        if (year != null) {
            this.year = year;
        } else this.year = Year.of(0);
    }

    public Year getYear() {
        return year;
    }

    public void setBitrate(long bitrate) {
        this.bitrate = Math.max(bitrate, 0);
    }

    public long getBitrate() {
        return bitrate;
    }

    public void setSamplerate(long samplerate) {
        this.samplerate = Math.max(samplerate, 0);
    }

    public long getSamplerate() {
        return samplerate;
    }

    public String getArtworkPath() {
        return artworkPath;
    }

    public void setArtworkPath(String artworkPath) {
        this.artworkPath = artworkPath;
    }

    public Path getArtworkFile() {
        return artworkPath != null ? Paths.get(artworkPath) : null;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getSeries() {
        return series;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getArtist() {
        return artist;
    }

    public void setSeriesArtist(String seriesArtist) {
        this.seriesArtist = seriesArtist;
    }

    public String getSeriesArtist() {
        return seriesArtist;
    }

    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Metadata)) return false;
        return id == ((Metadata) o).id;
    }

    @Override
    public String toString() {
        return "Metadata{" +
                ", \ntitle='" + title +
                ", \ngenre='" + genre +
                ", \ndurationInSeconds=" + durationInSeconds +
                ", \nyear=" + year +
                ", \nbitrate=" + bitrate +
                ", \nsampleRate=" + samplerate +
                ", \ndescription='" + description +
                ", \nlanguage='" + language +
                ", \nartist='" + this.getArtist() +
                ", \ntrackNumber" + this.getTrackNumber() +
                ", \nseries" + this.getSeries() +
                ", \nseriesArtist" + this.getSeriesArtist() +
                ", \nlyrics='" + lyrics +
                '}';
    }

    public String toText() {
        return "metadata object id: " + this.id + ", title: " + this.title;
    }
}