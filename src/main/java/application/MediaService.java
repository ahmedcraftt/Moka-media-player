package application;

import entities.Track;
import infrastructure.media.MediaScanner;
import mediaLibrary.MediaLibrary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MediaService {

    private final MediaScanner scanner;
    private final MediaLibrary library;
    private final LibraryService libraryService;

    public MediaService(MediaScanner scanner,
                        MediaLibrary library,
                        LibraryService libraryService) {
        this.scanner = scanner;
        this.library = library;
        this.libraryService = libraryService;
    }

    public List<Track> loadActiveLibrary() {

        Set<Track> tracks = new HashSet<>();

        for (var path : libraryService
                .getActiveLibrary()
                .getRootPaths()) {

            tracks.addAll(scanner.scan(path));
        }

        library.clear();
        library.addAll(new ArrayList<>(tracks));
        return library.getTracks();
    }

    public LibraryService getLibraryService() {
        return libraryService;
    }

    public List<Track> getSongs() {
        return library.getSongs();
    }

    public List<Track> getAudioBooks() {
        return library.getAudiobooks();
    }

    public List<Track> getPodcasts() {
        return library.getPodcasts();
    }

    public List<Track> getTracks() {
        return library.getTracks();
    }

}