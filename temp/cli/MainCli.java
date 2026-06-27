package cli;

import application.service.LibraryService;
import application.service.MediaService;
import infrastructure.audio.AudioEngine;
import infrastructure.audio.AudioPlayer;
import infrastructure.audio.VLCJAudioEngine;
import infrastructure.media.FiledataManager;
import infrastructure.media.JaudiotaggerManager;
import infrastructure.scanner.MediaScanner;
import infrastructure.media.MetadataManager;
import domain.model.library.MediaLibrary;
import infrastructure.storage.TrackStorage;

public final class MainCli {

    public static void main(String[] args) {
        AudioEngine engine = new VLCJAudioEngine();
        AudioPlayer player = new AudioPlayer(engine);
        MetadataManager metaDataManager = new JaudiotaggerManager();
        FiledataManager filedataManager = new FiledataManager();
        TrackStorage storage = new TrackStorage();
        MediaScanner scanner = new MediaScanner(metaDataManager, filedataManager, storage);
        MediaLibrary library = new MediaLibrary();
        LibraryService libraryService = new LibraryService();
        MediaService mediaService = new MediaService(scanner, library,libraryService);

        CliApp app = new CliApp(mediaService, player, libraryService);
        app.start();
    }
}