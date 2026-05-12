package domain.model;

import java.time.Year;

public class Metadata {

    private String title;
    private String genre;
    private int durationInSeconds;
    private byte[] artwork;
    private Year year;
    private long bitrate;
    private int sampleRate;
    private String description;
    private String artist;
    private String album;
    private String albumArtist;
    private String lyrics;
    private int episodeNumber;
    private String channel;
    private String host;
    private String author;
    private String narrator;
    private String series;
    private int chapterCount;

    public Metadata() {

    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getChapterCount() {
        return chapterCount;
    }

    public void setChapterCount(int chapterCount) {
        this.chapterCount = chapterCount;
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
                ", \nchapterCount=" + chapterCount +
                ", \nlyrics='" + lyrics + '\'' +
                '}';
    }
}
