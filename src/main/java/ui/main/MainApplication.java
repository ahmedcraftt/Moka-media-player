package ui.main;

import application.LibraryService;
import application.MediaService;
import infrastructure.audio.AudioEngine;
import infrastructure.audio.AudioPlayer;
import infrastructure.audio.VLCJAudioEngine;
import infrastructure.media.JaudiotaggerManager;
import infrastructure.media.MediaScanner;
import infrastructure.media.MetaDataManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mediaLibrary.MediaLibrary;
import ui.controllers.MainViewController;
import ui.controllers.PlaybackContext;


import java.io.IOException;

public class MainApplication extends Application {
    private final AudioEngine engine = new VLCJAudioEngine();
    private final AudioPlayer player = new AudioPlayer(engine);
    private final MetaDataManager metaDataManger = new JaudiotaggerManager();
    private final MediaScanner scanner = new MediaScanner(metaDataManger);
    private final MediaLibrary library = new MediaLibrary();
    private final LibraryService libraryService = new LibraryService();
    private final MediaService mediaService = new MediaService(scanner,library,libraryService);
    private final PlaybackContext playbackContext = new PlaybackContext();
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/mainView.fxml"));
        Parent root = loader.load();
            MainViewController controller = loader.getController();
            controller.setPlayer(player);
        controller.setPlaybackContext(playbackContext);
            controller.setMediaService(mediaService);
            controller.setLibraryService(libraryService);

        Scene scene = new Scene(root,1000,750);

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()){
                case P ->{
                    if (playbackContext.getSelectedTrack() != null) {
                        player.play(playbackContext.getSelectedTrack());
                        controller.updatePlayButton();
                    }
                }
                case SPACE -> player.pause();
                case RIGHT -> player.playNext();
                case LEFT -> player.playPrev();
                case UP -> player.setVolume(10);
                case DOWN -> player.setVolume(-10);
                case M -> player.setVolume(0);
                case F -> {
                    if (playbackContext.getSelectedTrack() != null) {
                        playbackContext.getSelectedTrack()
                                .setFavorite(!playbackContext
                                        .getSelectedTrack()
                                        .isFavorite());
                    }
                }
            }
        });

        stage.setTitle("Moka Player ☕");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setOnCloseRequest(
                event -> {
                    System.out.println("Closing app...");
                    engine.release();
                    event.consume();
                    Platform.exit();
                });
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
