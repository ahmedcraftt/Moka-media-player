package domain.model;

public interface MediaMetadata {
    String getSeries();

    void setSeries(String series);

    String getSeriesArtist();

    void setSeriesArtist(String seriesArtist);

    int getTrackNumber();

    void setTrackNumber(int trackNumber);

    String getArtist();

    void setArtist(String artist);
}
