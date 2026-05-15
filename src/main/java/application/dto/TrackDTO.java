package application.dto;

import java.nio.file.Path;

public class TrackDTO {
    private String title;
    private boolean favorite;
    private int timesPlayed;
    private String path;

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTimesPlayed(int timesPlayed) {
        this.timesPlayed = timesPlayed;
    }

    public int getTimesPlayed() {
        return timesPlayed;
    }
}
