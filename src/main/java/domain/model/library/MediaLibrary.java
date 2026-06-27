package domain.model.library;

import domain.model.media.MediaType;
import domain.model.media.Track;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class MediaLibrary {

    private final Map<Path, Track> tracks = new HashMap<>();

    public void addAll(List<Track> newTracks) {
        for (Track t : newTracks) {
            tracks.putIfAbsent(t.getFiledata().getFilePath(), t);
        }
    }

    public void addTrack(Track track) {
        tracks.putIfAbsent(track.getFiledata().getFilePath(), track);
    }

    public List<Track> getTracks() {
        return new ArrayList<>(tracks.values());
    }

    public void clear() {
        tracks.clear();
    }

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
                                safe(t.getFiledata().getFileName()).contains(q)
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