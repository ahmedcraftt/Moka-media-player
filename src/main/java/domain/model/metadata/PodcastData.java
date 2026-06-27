package domain.model.metadata;

public class PodcastData implements MediaMetadata {

    private int episodeNumber = 0;
    private String channel = "Unknown";
    private String host = "Unknown";
    private String series = "Unknown";

    @Override
    public String getArtist() {
        return host;
    }

    @Override
    public void setArtist(String artist) {
        this.host = artist;
    }

    @Override
    public String getSeries() {
        return series;
    }

    @Override
    public void setSeries(String series) {
        this.series = series;
    }

    @Override
    public String getSeriesArtist() {
        return channel;
    }

    @Override
    public void setSeriesArtist(String seriesArtist) {
        this.channel = seriesArtist;
    }

    @Override
    public int getTrackNumber() {
        return episodeNumber;
    }

    @Override
    public void setTrackNumber(int trackNumber) {
        this.episodeNumber = trackNumber;
    }
}
