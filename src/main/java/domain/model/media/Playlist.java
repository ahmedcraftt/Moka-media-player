package domain.model.media;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Playlist implements Displayable {

    private final List<Track> tracks = new ArrayList<>();
    private String title = "Unknown";
    private boolean favorite = false;

    public Playlist(String title) {
        this.title = title;
    }

    public Playlist() {}

    public Playlist(String title, boolean favorite) {
        this.title = title;
        this.favorite = favorite;
    }

    public boolean contains(Track track) {
        return tracks.contains(track);
    }

    public void addTrack(Track track) {
        this.tracks.add(track);
    }

    public void addTracks(List<Track> tracks) {
        this.tracks.addAll(tracks);
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

    @Override
    public byte[] getArtwork() {
        return tracks.getFirst().getArtwork();
    }

    public void setTitle(String title) {
        if (title == null) title = "Unknown";
        this.title = title;
    }

    public int getTotalDurationSeconds() {
        return tracks.stream().mapToInt(t->t.getMetadata().getDurationInSeconds()).sum();
    }

    @Override
    public String toString() {
        return "Playlist: " + title + " (" + size() + " tracks)";
    }
}