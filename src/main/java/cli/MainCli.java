package cli;

import application.LibraryService;
import application.MediaService;
import infrastructure.audio.AudioEngine;
import infrastructure.audio.AudioPlayer;
import infrastructure.audio.VLCJAudioEngine;
import infrastructure.media.JaudiotaggerManager;
import infrastructure.media.MediaScanner;
import infrastructure.media.MetaDataManager;
import mediaLibrary.MediaLibrary;

public class MainCli {

    public static void main(String[] args) {

        AudioEngine engine = new VLCJAudioEngine();
        AudioPlayer player = new AudioPlayer(engine);
        MetaDataManager metaDataManager = new JaudiotaggerManager();
        MediaScanner scanner = new MediaScanner(metaDataManager);
        MediaLibrary library = new MediaLibrary();
        LibraryService libraryService = new LibraryService();
        MediaService mediaService = new MediaService(scanner, library,libraryService);

        CliApp app = new CliApp(mediaService, player, libraryService);
        app.start();
    }
}