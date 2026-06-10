package domain.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Playlist {

    private final List<Track> tracks = new ArrayList<>();
    private String title = "Unknown";
    private boolean favorite = false;
    private Path playlistArtworkPath;

    public Playlist(String title) {
        this.title = title;
    }

    public Playlist() {}

    public Playlist(String title, boolean favorite) {
        this.title = title;
        this.favorite = favorite;
    }

    public Playlist(String title, boolean favorite, Path playlistArtworkPath) {
        this.title = title;
        this.favorite = favorite;
        setPlaylistArtworkPath(playlistArtworkPath);
    }

    public void addTrack(Track track) {
        tracks.add(track);
    }

    public boolean removeTrack(Track track) {
        return tracks.remove(track);
    }

    public Track getTrack(int index) {
        return tracks.get(index);
    }

    public List<Track> getTracks() {
        return Collections.unmodifiableList(tracks);
    }

    public int size() {
        return tracks.size();
    }

    public void clear() {
        tracks.clear();
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null) title = "Unknown";
        this.title = title;
    }

    public int getTotalDurationSeconds() {
        return tracks.stream().mapToInt(t->t.getMetadata().getDurationInSeconds()).sum();
    }

    public Path getPlaylistArtworkPath() {
        return playlistArtworkPath;
    }

    public void setPlaylistArtworkPath(Path playlistArtworkPath) {
        if (playlistArtworkPath == null) throw new IllegalArgumentException("playlistArtworkPath cannot be null");
        this.playlistArtworkPath = playlistArtworkPath;
    }

    @Override
    public String toString() {
        return "Playlist: " + title + " (" + size() + " tracks)";
    }
}