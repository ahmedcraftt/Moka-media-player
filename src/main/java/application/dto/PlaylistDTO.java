package application.dto;

import java.util.List;

public class PlaylistDTO {
    public String title;
    public boolean favorite;
    public List<String> trackPaths;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public List<String> getTrackPaths() {
        return trackPaths;
    }

    public void setTrackPaths(List<String> trackPaths) {
        this.trackPaths = trackPaths;
    }

}
