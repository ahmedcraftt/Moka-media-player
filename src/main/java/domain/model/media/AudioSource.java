package domain.model.media;

import java.net.URI;

public interface AudioSource {
    URI getResource();

    String getTitle();

    boolean isFavorite();

    void setFavorite(boolean favorite);
}
