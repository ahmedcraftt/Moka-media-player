package config;

import gui.controllers.SortByModes;
import gui.controllers.ViewMode;
import javafx.scene.text.TextAlignment;

import java.time.LocalDateTime;

public final class UIConfig {
    private static final int DEFAULT_WIDTH = 400;
    private static final int DEFAULT_HEIGHT = 400;
    private static final int DEFAULT_SPEED = 64;
    private static final int DEFAULT_CUTOFF_Days = 30;
    private static final ViewMode DEFAULT_STARTING_VIEW_MODE = ViewMode.TRACKS;
    private static final TextAlignment DEFAULT_TEXT_ALIGNMENT = TextAlignment.CENTER;
    private static final SortByModes DEFAULT_STARTING_SORT_BY_MODE = SortByModes.TITLE;

    private boolean tracksBtnVisibility = true;
    private boolean songsBtnVisibility = true;
    private boolean booksBtnVisibility = true;
    private boolean podcastsBtnVisibility = true;
    private boolean artistsBtnVisibility = true;
    private boolean albumsBtnVisibility = true;
    private boolean genresBtnVisibility = true;
    private boolean playlistsBtnVisibility = true;

    private int artworkImageWidth = DEFAULT_WIDTH;
    private int artworkImageHeight = DEFAULT_HEIGHT;
    private int artworkRotationSpeed = DEFAULT_SPEED;
    private int cutoffDays = DEFAULT_CUTOFF_Days;

    private ViewMode startingViewMode = DEFAULT_STARTING_VIEW_MODE;
    private TextAlignment lyricsTextAlignment = DEFAULT_TEXT_ALIGNMENT;
    private SortByModes startingSortByMode = DEFAULT_STARTING_SORT_BY_MODE;

    public boolean isTracksBtnVisibility() {
        return tracksBtnVisibility;
    }

    public void setTracksBtnVisibility(boolean tracksBtnVisibility) {
        this.tracksBtnVisibility = tracksBtnVisibility;
    }

    public boolean isSongsBtnVisibility() {
        return songsBtnVisibility;
    }

    public void setSongsBtnVisibility(boolean songsBtnVisibility) {
        this.songsBtnVisibility = songsBtnVisibility;
    }

    public boolean isBooksBtnVisibility() {
        return booksBtnVisibility;
    }

    public void setBooksBtnVisibility(boolean booksBtnVisibility) {
        this.booksBtnVisibility = booksBtnVisibility;
    }

    public boolean isPodcastsBtnVisibility() {
        return podcastsBtnVisibility;
    }

    public void setPodcastsBtnVisibility(boolean podcastsBtnVisibility) {
        this.podcastsBtnVisibility = podcastsBtnVisibility;
    }

    public boolean isArtistsBtnVisibility() {
        return artistsBtnVisibility;
    }

    public void setArtistsBtnVisibility(boolean artistsBtnVisibility) {
        this.artistsBtnVisibility = artistsBtnVisibility;
    }

    public boolean isAlbumsBtnVisibility() {
        return albumsBtnVisibility;
    }

    public void setAlbumsBtnVisibility(boolean albumsBtnVisibility) {
        this.albumsBtnVisibility = albumsBtnVisibility;
    }

    public boolean isGenresBtnVisibility() {
        return genresBtnVisibility;
    }

    public void setGenresBtnVisibility(boolean genresBtnVisibility) {
        this.genresBtnVisibility = genresBtnVisibility;
    }

    public boolean isPlaylistsBtnVisibility() {
        return playlistsBtnVisibility;
    }

    public void setPlaylistsBtnVisibility(boolean playlistsBtnVisibility) {
        this.playlistsBtnVisibility = playlistsBtnVisibility;
    }

    public int getArtworkImageWidth() {
        return artworkImageWidth;
    }

    public void setArtworkImageWidth(int artworkImageWidth) {
        this.artworkImageWidth = artworkImageWidth;
    }

    public int getArtworkImageHeight() {
        return artworkImageHeight;
    }

    public void setArtworkImageHeight(int artworkImageHeight) {
        this.artworkImageHeight = artworkImageHeight;
    }

    public int getArtworkRotationSpeed() {
        return artworkRotationSpeed;
    }

    public void setArtworkRotationSpeed(int artworkRotationSpeed) {
        this.artworkRotationSpeed = artworkRotationSpeed;
    }

    public ViewMode getStartingViewMode() {
        return startingViewMode;
    }

    public void setStartingViewMode(ViewMode startingViewMode) {
        this.startingViewMode = startingViewMode;
    }

    public TextAlignment getLyricsTextAlignment() {
        return lyricsTextAlignment;
    }

    public void setLyricsTextAlignment(TextAlignment lyricsTextAlignment) {
        this.lyricsTextAlignment = lyricsTextAlignment;
    }

    public SortByModes getStartingSortByMode() {
        return startingSortByMode;
    }

    public void setStartingSortByMode(SortByModes startingSortByMode) {
        this.startingSortByMode = startingSortByMode;
    }

    public int getCutoffDays() {
        return cutoffDays;
    }

    public void setCutoffDays(int cutoffDays) {
        this.cutoffDays = cutoffDays;
    }
}
