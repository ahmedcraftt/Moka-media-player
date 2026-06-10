package domain.model;

public class SongData implements MediaMetadata {
    private int albumNumber = 0;
    private String artist = "Unknown";
    private String album = "Unknown";
    private String albumArtist = "Unknown";

    public String getSeries() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        if (!artist.isBlank() && artist != null) {
            this.artist = artist;
        } else this.artist = "Unknown";
    }

    public void setSeries(String series) {
        this.album = series;
    }

    @Override
    public String getSeriesArtist() {
        return albumArtist;
    }

    @Override
    public void setSeriesArtist(String seriesArtist) {
        this.albumArtist = seriesArtist;
    }

    @Override
    public int getTrackNumber() {
        return albumNumber;
    }

    @Override
    public void setTrackNumber(int trackNumber) {
        this.albumNumber = trackNumber;
    }
}
