package config;

import domain.audio.RepeatMode;
import gui.controllers.SearchEngine;
import gui.controllers.SortByModes;
import gui.controllers.ViewMode;
import javafx.scene.text.TextAlignment;

public final class AppConfig {

    private int configVersion = 1;
    private PlayerConfig playerConfig = new PlayerConfig();
    private UIConfig uiConfig = new UIConfig();
    private SearchConfig searchConfig = new SearchConfig();

    public PlayerConfig getPlayerConfig() {
        return playerConfig;
    }

    public UIConfig getUIConfig() {
        return uiConfig;
    }

    public int getPreferredSkipSeconds() {
        return playerConfig.getPreferredSkipSeconds();
    }

    public void setPreferredSkipSeconds(int preferredSkipSeconds) {
        this.playerConfig.setPreferredSkipSeconds(preferredSkipSeconds);
    }

    public int getPreferredVolumeLevel() {
        return playerConfig.getPreferredVolumeLevel();
    }

    public void setPreferredVolumeLevel(int preferredVolumeLevel) {
        this.playerConfig.setPreferredVolumeLevel(preferredVolumeLevel);
    }

    public int getPreferredVolumeModifier() {
        return playerConfig.getPreferredVolumeModifier();
    }

    public void setPreferredVolumeModifier(int preferredVolumeModifier) {
        this.playerConfig.setPreferredVolumeModifier(preferredVolumeModifier);
    }

    public boolean isTracksBtnVisibility() {
        return uiConfig.isTracksBtnVisibility();
    }

    public void setTracksBtnVisibility(boolean tracksBtnVisibility) {
        this.uiConfig.setTracksBtnVisibility(tracksBtnVisibility);
    }

    public boolean isSongsBtnVisibility() {
        return uiConfig.isSongsBtnVisibility();
    }

    public void setSongsBtnVisibility(boolean songsBtnVisibility) {
        this.uiConfig.setSongsBtnVisibility(songsBtnVisibility);
    }

    public boolean isBooksBtnVisibility() {
        return uiConfig.isBooksBtnVisibility();
    }

    public void setBooksBtnVisibility(boolean booksBtnVisibility) {
        this.uiConfig.setBooksBtnVisibility(booksBtnVisibility);
    }

    public boolean isPodcastsBtnVisibility() {
        return uiConfig.isPodcastsBtnVisibility();
    }

    public void setPodcastsBtnVisibility(boolean podcastsBtnVisibility) {
        this.uiConfig.setPodcastsBtnVisibility(podcastsBtnVisibility);
    }

    public boolean isArtistsBtnVisibility() {
        return uiConfig.isArtistsBtnVisibility();
    }

    public void setArtistsBtnVisibility(boolean artistsBtnVisibility) {
        this.uiConfig.setArtistsBtnVisibility(artistsBtnVisibility);
    }

    public boolean isAlbumsBtnVisibility() {
        return uiConfig.isAlbumsBtnVisibility();
    }

    public void setAlbumsBtnVisibility(boolean albumsBtnVisibility) {
        this.uiConfig.setAlbumsBtnVisibility(albumsBtnVisibility);
    }

    public boolean isGenresBtnVisibility() {
        return uiConfig.isGenresBtnVisibility();
    }

    public void setGenresBtnVisibility(boolean genresBtnVisibility) {
        this.uiConfig.setGenresBtnVisibility(genresBtnVisibility);
    }

    public boolean isPlaylistsBtnVisibility() {
        return uiConfig.isPlaylistsBtnVisibility();
    }

    public void setPlaylistsBtnVisibility(boolean playlistsBtnVisibility) {
        this.uiConfig.setPlaylistsBtnVisibility(playlistsBtnVisibility);
    }

    public int getArtworkImageHeight() {
        return uiConfig.getArtworkImageHeight();
    }

    public void setArtworkImageHeight(int artworkImageHeight) {
        this.uiConfig.setArtworkImageHeight(artworkImageHeight);
    }

    public int getArtworkImageWidth() {
        return uiConfig.getArtworkImageWidth();
    }

    public void setArtworkImageWidth(int artworkImageWidth) {
        this.uiConfig.setArtworkImageWidth(artworkImageWidth);
    }

    public int getArtworkImageRotationSpeed() {
        return uiConfig.getArtworkRotationSpeed();
    }

    public void setArtworkImageRotationSpeed(int artworkImageRotationSpeed) {
        this.uiConfig.setArtworkRotationSpeed(artworkImageRotationSpeed);
    }

    public RepeatMode getPrefferredRepeatMode() {
        return playerConfig.getPreferredRepeatMode();
    }

    public void setPreferredRepeatMode(RepeatMode prefferredRepeatMode) {
        this.playerConfig.setPreferredRepeatMode(prefferredRepeatMode);
    }

    public boolean isShuffle() {
        return playerConfig.isShuffle();
    }

    public void setShuffle(boolean shuffle) {
        this.playerConfig.setShuffle(shuffle);
    }

    public ViewMode getViewMode() {
        return uiConfig.getStartingViewMode();
    }

    public void setViewMode(ViewMode viewMode) {
        this.uiConfig.setStartingViewMode(viewMode);
    }

    public TextAlignment getTextAlignment() {
        return uiConfig.getLyricsTextAlignment();
    }

    public void setTextAlignment(TextAlignment textAlignment) {
        this.uiConfig.setLyricsTextAlignment(textAlignment);
    }

    public SortByModes getSortByMode() {
        return uiConfig.getStartingSortByMode();
    }

    public void setSortByMode(SortByModes sortByMode) {
        this.uiConfig.setStartingSortByMode(sortByMode);
    }

    public SearchEngine getSearchEngine() {
        return searchConfig.getPreferredSearchEngine();
    }

    public void setSearchEngine(SearchEngine searchEngine) {
        this.searchConfig.setPreferredSearchEngine(searchEngine);
    }

}
