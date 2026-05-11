package domain.library;

import domain.model.MediaType;
import domain.model.Track;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class MediaLibrary {

    private final Map<String, Track> tracks = new HashMap<>();

    public void addAll(List<Track> newTracks) {
        for (Track t : newTracks) {
            tracks.putIfAbsent(t.getFilePath().toString(), t);
        }
    }

    public void addTrack(Track track) {
        tracks.putIfAbsent(track.getFilePath().toString(), track);
    }

    public List<Track> getTracks() {
        return new ArrayList<>(tracks.values());
    }

    public void removeTrack(@NotNull Track track) {
        tracks.remove(track.getFilePath().toString());
    }

    public void clear() {
        tracks.clear();
    }

    // ===== QUERY LAYER =====

    public List<Track> getSongs() {
        return filterByType(MediaType.SONG);
    }

    public List<Track> getPodcasts() {
        return filterByType(MediaType.PODCAST);
    }

    public List<Track> getAudiobooks() {
        return filterByType(MediaType.AUDIOBOOK);
    }

    private List<Track> filterByType(MediaType type) {
        return tracks.values().stream()
                .filter(t -> t.getType()==type)
                .collect(Collectors.toList());
    }

    public List<Track> search(String query) {
        String q = query.toLowerCase();

        return tracks.values().stream()
                .filter(t ->
                        safe(t.getMetadata().getTitle()).contains(q) ||
                                safe(t.getMetadata().getGenre()).contains(q) ||
                                safe(t.getFileName()).contains(q)
                )
                .collect(Collectors.toList());
    }

    public List<Track> sortByTitle() {
        return sort(Comparator.comparing(t -> safe(t.getMetadata().getTitle())));
    }

    public List<Track> sortByDuration() {
        return sort(Comparator.comparingInt(track -> track.getMetadata().getDurationInSeconds()));
    }

    private List<Track> sort(Comparator<Track> comparator) {
        return tracks.values().stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private String safe(String s) {
        return s == null ? "" : s.toLowerCase();
    }
}