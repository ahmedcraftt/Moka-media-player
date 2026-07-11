package application.service;

import domain.model.library.Library;
import domain.model.library.MediaLibrary;
import domain.model.media.Playlist;
import domain.model.media.Track;
import infrastructure.scanner.MediaScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class MediaService {

    private static final Logger logger = LoggerFactory.getLogger(MediaService.class);

    private final MediaScanner scanner;
    private final MediaLibrary mediaLibrary;
    private final LibraryService libraryService;

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
        reloadActiveLibrary();
    }

    public void refreshActiveLibrary() {
        reloadActiveLibrary();
    }

    private void reloadActiveLibrary() {
        Library activeLibrary = libraryService.getActiveLibrary();

        if (activeLibrary == null) {
            logger.warn("No active library selected.");
            return;
        }

        Set<Track> uniqueTracks = new HashSet<>();

        for (var folder : activeLibrary.getRootPaths()) {
            List<Track> scanned = scanner.scan(folder);
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