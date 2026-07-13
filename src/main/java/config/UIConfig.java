package config;

import gui.controllers.SortByModes;
import gui.controllers.ViewMode;
import javafx.scene.text.TextAlignment;

public final class UIConfig {
    private static final int DEFAULT_WIDTH = 400;
    private static final int DEFAULT_HEIGHT = 400;
    private static final int DEFAULT_SPEED = 64;
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

    private ViewMode startingViewMode = DEFAULT_STARTING_VIEW_MODE;
    private TextAlignment lyricsTextAlignment = DEFAULT_TEXT_ALIGNMENT;
    private SortByModes startingSortByMode = DEFAULT_STARTING_SORT_BY_MODE;

    boolean isTracksBtnVisibility() {
        return tracksBtnVisibility;
    }

    void setTracksBtnVisibility(boolean tracksBtnVisibility) {
        this.tracksBtnVisibility = tracksBtnVisibility;
    }

    boolean isSongsBtnVisibility() {
        return songsBtnVisibility;
    }

    void setSongsBtnVisibility(boolean songsBtnVisibility) {
        this.songsBtnVisibility = songsBtnVisibility;
    }

    boolean isBooksBtnVisibility() {
        return booksBtnVisibility;
    }

    void setBooksBtnVisibility(boolean booksBtnVisibility) {
        this.booksBtnVisibility = booksBtnVisibility;
    }

    boolean isPodcastsBtnVisibility() {
        return podcastsBtnVisibility;
    }

    void setPodcastsBtnVisibility(boolean podcastsBtnVisibility) {
        this.podcastsBtnVisibility = podcastsBtnVisibility;
    }

    boolean isArtistsBtnVisibility() {
        return artistsBtnVisibility;
    }

    void setArtistsBtnVisibility(boolean artistsBtnVisibility) {
        this.artistsBtnVisibility = artistsBtnVisibility;
    }

    boolean isAlbumsBtnVisibility() {
        return albumsBtnVisibility;
    }

    void setAlbumsBtnVisibility(boolean albumsBtnVisibility) {
        this.albumsBtnVisibility = albumsBtnVisibility;
    }

    boolean isGenresBtnVisibility() {
        return genresBtnVisibility;
    }

    void setGenresBtnVisibility(boolean genresBtnVisibility) {
        this.genresBtnVisibility = genresBtnVisibility;
    }

    boolean isPlaylistsBtnVisibility() {
        return playlistsBtnVisibility;
    }

    void setPlaylistsBtnVisibility(boolean playlistsBtnVisibility) {
        this.playlistsBtnVisibility = playlistsBtnVisibility;
    }

    int getArtworkImageWidth() {
        return artworkImageWidth;
    }

    void setArtworkImageWidth(int artworkImageWidth) {
        this.artworkImageWidth = artworkImageWidth;
    }

    int getArtworkImageHeight() {
        return artworkImageHeight;
    }

    void setArtworkImageHeight(int artworkImageHeight) {
        this.artworkImageHeight = artworkImageHeight;
    }

    int getArtworkRotationSpeed() {
        return artworkRotationSpeed;
    }

    void setArtworkRotationSpeed(int artworkRotationSpeed) {
        this.artworkRotationSpeed = artworkRotationSpeed;
    }

    ViewMode getStartingViewMode() {
        return startingViewMode;
    }

    void setStartingViewMode(ViewMode startingViewMode) {
        this.startingViewMode = startingViewMode;
    }

    TextAlignment getLyricsTextAlignment() {
        return lyricsTextAlignment;
    }

    void setLyricsTextAlignment(TextAlignment lyricsTextAlignment) {
        this.lyricsTextAlignment = lyricsTextAlignment;
    }

    SortByModes getStartingSortByMode() {
        return startingSortByMode;
    }

    void setStartingSortByMode(SortByModes startingSortByMode) {
        this.startingSortByMode = startingSortByMode;
    }
}
