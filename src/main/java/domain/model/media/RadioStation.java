package domain.model.media;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class RadioStation implements AudioSource, Displayable {

    private URL stationAddress;
    private URI stationImage;
    private String title;
    private String description;
    private String genre;
    private boolean favorite;

    @Override
    public URI getResource() {
        try {
            return stationAddress.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getArtworkPath() {
        return stationImage.toString();
    }

    @Override
    public boolean isFavorite() {
        return favorite;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getGenre() {
        return genre;
    }

    @Override
    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public void setStationAddress(URL stationAddress) {
        this.stationAddress = stationAddress;
    }

    public void setStationImage(URI stationImage) {
        this.stationImage = stationImage;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

}
