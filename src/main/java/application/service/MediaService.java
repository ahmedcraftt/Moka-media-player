package application.service;

import domain.model.library.MediaLibrary;
import domain.model.media.Track;
import infrastructure.scanner.MediaScanner;

import java.util.*;
import java.util.stream.Collectors;

public class MediaService {

    private final MediaScanner scanner;
    private final MediaLibrary mediaLibrary;
    private final LibraryService libraryService;

    private List<List<Track>> cachedAlbums = new ArrayList<>();
    private List<List<Track>> cachedArtists = new ArrayList<>();
    private List<List<Track>> cachedGenres = new ArrayList<>();

    public MediaService(MediaScanner scanner,
                        MediaLibrary mediaLibrary,
                        LibraryService libraryService) {
        this.scanner = scanner;
        this.mediaLibrary = mediaLibrary;
        this.libraryService = libraryService;
    }

    public void loadActiveLibrary() {
        if (libraryService.getActiveLibrary() == null) {
            System.err.println("WARN: Attempted to load library, but no active library selection exists.");
            return;
        }

        Set<Track> uniqueTracks = new HashSet<>();
        for (var path : libraryService.getActiveLibrary().getRootPaths()) {
            List<Track> scanned = scanner.scan(path);
            if (scanned != null) {
                uniqueTracks.addAll(scanned);
            }
        }

        mediaLibrary.clear();
        mediaLibrary.addAll(new ArrayList<>(uniqueTracks));

        rebuildMetadataCaches();
    }

    private void rebuildMetadataCaches() {
        List<Track> allTracks = getTracks();

        this.cachedAlbums = allTracks.stream()
                .collect(Collectors.groupingBy(track -> {
                    if (track.getMetadata() == null || track.getMediaMetadata().getSeries() == null) {
                        return "Unknown Album";
                    }
                    String series = track.getMediaMetadata().getSeries().trim();
                    return series.isEmpty() ? "Unknown Album" : series;
                }))
                .values().stream().toList();

        this.cachedArtists = allTracks.stream()
                .collect(Collectors.groupingBy(track -> {
                    if (track.getMetadata() == null || track.getMediaMetadata().getArtist() == null) {
                        return "Unknown Artist";
                    }
                    String artist = track.getMediaMetadata().getArtist().trim();
                    return artist.isEmpty() ? "Unknown Artist" : artist;
                }))
                .values().stream().toList();

        this.cachedGenres = allTracks.stream()
                .collect(Collectors.groupingBy(track -> {
                    if (track.getMetadata() == null || track.getMetadata().getGenre() == null) {
                        return "Unknown Genre";
                    }
                    String genre = track.getMetadata().getGenre().trim();
                    return genre.isEmpty() ? "Unknown Genre" : genre;
                }))
                .values().stream().toList();
    }

    public LibraryService getLibraryService() {
        return libraryService;
    }

    public List<Track> getSongs() {
        return mediaLibrary.getSongs();
    }

    public List<Track> getAudioBooks() {
        return mediaLibrary.getAudiobooks();
    }

    public List<Track> getPodcasts() {
        return mediaLibrary.getPodcasts();
    }

    public List<Track> getTracks() {
        return mediaLibrary.getTracks();
    }

    public List<List<Track>> getAlbums() {
        return cachedAlbums;
    }

    public List<List<Track>> getArtists() {
        return cachedArtists;
    }

    public List<List<Track>> getGenre() {
        return cachedGenres;
    }
}