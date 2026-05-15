package domain.model;

import java.time.Year;

public class Metadata {

    private String title = "Unknown";
    private String genre = "Unknown";
    private int durationInSeconds = 0;
    private byte[] artwork;
    private Year year = Year.of(0);
    private long bitrate = 0;
    private int sampleRate = 0;
    private String description = "Unknown";
    private String artist = "Unknown";
    private String album = "Unknown";
    private String albumArtist = "Unknown";
    private String lyrics = "Unknown";
    private int episodeNumber = 0;
    private String channel = "Unknown";
    private String host = "Unknown";
    private String author = "Unknown";
    private String narrator = "Unknown";
    private String series = "Unknown";
    private int chapterNumber = 0;
    private int numberInAlbum = 0;


    public Metadata() {
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {

        this.album = album;
    }

    public int getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(int chapterNumber) {
        this.chapterNumber = chapterNumber;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(int episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getNarrator() {
        return narrator;
    }

    public void setNarrator(String narrator) {
        this.narrator = narrator;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getDescription() {
        return description;
    }

    public String getAlbumArtist() {
        return albumArtist;
    }

    public void setAlbumArtist(String albumArtist) {
        this.albumArtist = albumArtist;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(int durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    public byte[] getArtwork() {
        return artwork;
    }

    public void setArtwork(byte[] artwork) {
        this.artwork = artwork;
    }

    public Year getYear() {
        return year;
    }

    public int getYearNumber() {
        if (this.year != null) return year.getValue();
        else return 0;
    }

    public String getYearSting() {
        if (this.year != null) return year.toString();
        else return "unknown";
    }

    public void setYear(Year year) {
        this.year = year;
    }

    public long getBitrate() {
        return bitrate;
    }

    public void setBitrate(long bitrate) {
        this.bitrate = bitrate;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getNumberInAlbum() {
        return numberInAlbum;
    }

    public void setNumberInAlbum(int numberInAlbum) {
        this.numberInAlbum = numberInAlbum;
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "album='" + album + '\'' +
                ", title='" + title + '\'' +
                ", genre='" + genre + '\'' +
                ", durationInSeconds=" + durationInSeconds +
                ", \nyear=" + year +
                ", bitrate=" + bitrate +
                ", sampleRate=" + sampleRate +
                ", description='" + description + '\'' +
                ", \nartist='" + artist + '\'' +
                ", albumArtist='" + albumArtist + '\'' +
                ", episodeNumber=" + episodeNumber +
                ", channel='" + channel + '\'' +
                ", \nhost='" + host + '\'' +
                ", author='" + author + '\'' +
                ", narrator='" + narrator + '\'' +
                ", series='" + series + '\'' +
                ", \nchapterNumber=" + chapterNumber +
                ", album number=" + numberInAlbum +
                ", \nlyrics='" + lyrics + '\'' +
                '}';
    }
}
