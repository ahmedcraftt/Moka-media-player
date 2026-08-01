package gui.main;

import application.service.AppState;
import application.service.LibraryService;
import application.service.MediaService;
import application.service.PlayerService;
import config.AppConfig;
import config.storage.ConfigStorage;
import domain.model.library.MediaLibrary;
import infrastructure.audio.AudioEngine;
import infrastructure.audio.AudioPlayer;
import infrastructure.audio.VLCJAudioEngine;
import infrastructure.media.FiledataManager;
import infrastructure.media.JaudiotaggerManager;
import infrastructure.media.LyricsEmbedder;
import infrastructure.media.MetadataManager;
import infrastructure.scanner.DesktopMediaScanner;
import infrastructure.scanner.MediaScanner;
import infrastructure.storage.ArtworkStorage;
import infrastructure.storage.MetadataStorage;
import infrastructure.storage.TrackStorage;

public final class AppContext {

    private final MediaService mediaService;
    private final AudioPlayer audioPlayer;
    private final LibraryService libraryService;
    private final PlayerService playerService;
    private final MetadataManager metadataManager;
    private final MediaLibrary mediaLibrary;
    private final AppState appState;
    private final ArtworkStorage artStorage;
    private final MetadataStorage metadataStorage;
    private final TrackStorage trackStorage;
    private final MediaScanner scanner;
    private final FiledataManager filedataManager;
    private final AudioEngine audioEngine;
    private final AppConfig config;
    private final LyricsEmbedder lyricsEmbedder;

    public AppContext() {
        filedataManager = new FiledataManager();
        metadataManager = new JaudiotaggerManager();
        artStorage = new ArtworkStorage();
        metadataStorage = new MetadataStorage();
        mediaLibrary = new MediaLibrary();
        libraryService = new LibraryService();
        audioEngine = new VLCJAudioEngine();
        config = ConfigStorage.load();
        trackStorage = new TrackStorage(metadataStorage);
        lyricsEmbedder = new LyricsEmbedder(metadataManager);
        scanner = new DesktopMediaScanner(
                metadataManager,
                filedataManager,
                trackStorage,
                metadataStorage,
                artStorage,
                lyricsEmbedder
        );
        mediaService = new MediaService(
                scanner,
                mediaLibrary,
                libraryService,
                lyricsEmbedder
        );
        audioPlayer = new AudioPlayer(audioEngine);
        playerService = new PlayerService(audioPlayer);
        appState = new AppState(config);
    }

    public AppConfig config() {
        return config;
    }

    public AudioEngine audioEngine() {
        return audioEngine;
    }

    public FiledataManager filedataManager() {
        return filedataManager;
    }

    public MediaService mediaService() {
        return mediaService;
    }

    public AudioPlayer player() {
        return audioPlayer;
    }

    public LibraryService libraryService() {
        return libraryService;
    }

    public PlayerService playerService() {
        return playerService;
    }

    public MetadataManager metadataManager() {
        return metadataManager;
    }

    public MediaLibrary mediaLibrary() {
        return mediaLibrary;
    }

    public AppState appState() {
        return appState;
    }

    public ArtworkStorage artStorage() {
        return artStorage;
    }

    public MetadataStorage metadataStorage() {
        return metadataStorage;
    }

    public TrackStorage trackStorage() {
        return trackStorage;
    }

    public MediaScanner mediaScanner() {
        return scanner;
    }

    public LyricsEmbedder lyricsEmbedder() {
        return lyricsEmbedder;
    }

}
