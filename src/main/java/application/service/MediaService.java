package application.service;

import domain.model.library.MediaLibrary;
import domain.model.media.Playlist;
import domain.model.media.Track;
import infrastructure.scanner.MediaScanner;

import java.util.*;
import java.util.stream.Collectors;

public class MediaService {

    private final MediaScanner scanner;
    private final MediaLibrary mediaLibrary;
    private final LibraryService libraryService;

    private List<Track> cachedTracks = new ArrayList<>();
    private List<Track> cachedSongs = new ArrayList<>();
    private List<Track> cachedPodcasts = new ArrayList<>();
    private List<Track> cachedAudioBooks = new ArrayList<>();

    private List<Playlist> cachedAlbums = new ArrayList<>();
    private List<Playlist> cachedArtists = new ArrayList<>();
    private List<Playlist> cachedGenres = new ArrayList<>();

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

        this.cachedTracks = mediaLibrary.getTracks();
        this.cachedPodcasts = mediaLibrary.getPodcasts();
        this.cachedSongs = mediaLibrary.getSongs();
        this.cachedAudioBooks = mediaLibrary.getAudiobooks();
        IO.println("MediaService.java line 60:" + this.cachedTracks.size());

        List<Track> allTracks = getTracks();

        this.cachedAlbums = allTracks.stream()
                .collect(Collectors.groupingBy(track -> {
                    if (track.getMetadata() == null || track.getMetadata().getSeries() == null) {
                        return "Unknown Album";
                    }
                    String series = track.getMetadata().getSeries().trim();
                    return series.isEmpty() ? "Unknown Album" : series;
                }))
                .entrySet().stream()
                .map(entry -> {
                    Playlist albumPlaylist = new Playlist(entry.getKey());
                    albumPlaylist.addTracks(entry.getValue());
                    return albumPlaylist;
                })
                .collect(Collectors.toList());

        this.cachedArtists = allTracks.stream()
                .collect(Collectors.groupingBy(track -> {
                    if (track.getMetadata() == null || track.getMetadata().getArtist() == null) {
                        return "Unknown Artist";
                    }
                    String artist = track.getMetadata().getArtist().trim();
                    return artist.isEmpty() ? "Unknown Artist" : artist;
                }))
                .entrySet().stream()
                .map(entry -> {
                    Playlist artistPlaylist = new Playlist(entry.getKey());
                    artistPlaylist.addTracks(entry.getValue());
                    return artistPlaylist;
                })
                .collect(Collectors.toList());

        this.cachedGenres = allTracks.stream()
                .collect(Collectors.groupingBy(track -> {
                    if (track.getMetadata() == null || track.getMetadata().getGenre() == null) {
                        return "Unknown Genre";
                    }
                    String genre = track.getMetadata().getGenre().trim();
                    return genre.isEmpty() ? "Unknown Genre" : genre;
                }))
                .entrySet().stream()
                .map(entry -> {
                    Playlist genrePlaylist = new Playlist(entry.getKey());
                    genrePlaylist.addTracks(entry.getValue());
                    return genrePlaylist;
                })
                .collect(Collectors.toList());
    }

    public void refreshMetadataCaches() {
        this.cachedTracks = mediaLibrary.getTracks();
        this.cachedPodcasts = mediaLibrary.getPodcasts();
        this.cachedSongs = mediaLibrary.getSongs();
        this.cachedAudioBooks = mediaLibrary.getAudiobooks();
    }

    public LibraryService getLibraryService() {
        return libraryService;
    }

    public List<Track> getSongs() {
        return cachedSongs;
    }

    public List<Track> getAudioBooks() {
        return cachedAudioBooks;
    }

    public List<Track> getPodcasts() {
        return cachedPodcasts;
    }

    public List<Track> getTracks() {
        return cachedTracks;
    }


    public List<Playlist> getAlbums() {
        return cachedAlbums;
    }

    public List<Playlist> getArtists() {
        return cachedArtists;
    }

    public List<Playlist> getGenre() {
        return cachedGenres;
    }
}