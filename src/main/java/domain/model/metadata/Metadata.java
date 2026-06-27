package domain.model.metadata;

import java.time.Year;

public class Metadata {

    private int durationInSeconds = 0;
    private long bitrate = 0;
    private long sampleRate = 0;
    private String title = "Unknown";
    private String genre = "Unknown";
    private String description = "Unknown";
    private String lyrics = "Unknown";
    private String language = "Unknown";
    private Year year = Year.of(0);
    private byte[] artwork;

    public Metadata() {
    }


    public void setLyrics(String lyrics) {
        if (!lyrics.isBlank() && lyrics != null) {
            this.lyrics = lyrics;
        } else this.lyrics = "Unknown";
    }

    public String getLyrics() {
        return lyrics;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (!description.isBlank() && description != null) {
            this.description = description;
        } else this.description = "Unknown";
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        if (!genre.isBlank() && genre != null) {
            this.genre = genre;
        } else this.genre = "Unknown";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (!title.isBlank() && title != null) {
            this.title = title;
        } else this.title = "Unknown";
    }

    public int getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(int durationInSeconds) {
        this.durationInSeconds = Math.max(durationInSeconds, 0);
    }

    public byte[] getArtwork() {
        return artwork;
    }

    public void setArtwork(byte[] artwork) {
        this.artwork = artwork;
    }

    public void setLanguage(String language) {
        if (!language.isBlank() && language != null) {
            this.language = language;
        } else this.language = "Unknown";
    }

    public String getLanguage() {
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

    public void setSampleRate(long sampleRate) {
        this.sampleRate = Math.max(sampleRate, 0);
    }

    public long getSampleRate() {
        return sampleRate;
    }

    @Override
    public String toString() {
        return "Metadata{" +
                ", \ntitle='" + title +
                ", \ngenre='" + genre +
                ", \ndurationInSeconds=" + durationInSeconds +
                ", \nyear=" + year +
                ", \nbitrate=" + bitrate +
                ", \nsampleRate=" + sampleRate +
                ", \ndescription='" + description +
                ", \nlanguage='" + language +
                ", \nlyrics='" + lyrics +
                '}';
    }
}
