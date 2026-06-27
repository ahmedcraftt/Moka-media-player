package domain.model.metadata;

public class AudioBookData implements MediaMetadata {

    private int chapterNumber = 0;
    private String Author = "Unknown";
    private String narrator = "Unknown";
    private String bookSeries = "Unknown";

    @Override
    public String getArtist() {
        return narrator;
    }

    @Override
    public void setArtist(String artist) {
        this.narrator = artist;
    }

    @Override
    public String getSeries() {
        return bookSeries;
    }

    @Override
    public void setSeries(String series) {
        this.bookSeries = series;
    }

    @Override
    public String getSeriesArtist() {
        return Author;
    }

    @Override
    public void setSeriesArtist(String seriesArtist) {
        this.Author = seriesArtist;
    }

    @Override
    public int getTrackNumber() {
        return chapterNumber;
    }

    @Override
    public void setTrackNumber(int trackNumber) {
        this.chapterNumber = trackNumber;
    }
}
