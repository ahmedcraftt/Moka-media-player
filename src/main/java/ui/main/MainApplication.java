package ui.main;

import application.LibraryService;
import application.MediaService;
import application.PlayerService;
import infrastructure.audio.AudioEngine;
import infrastructure.audio.AudioPlayer;
import infrastructure.audio.PlaybackState;
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


import java.io.IOException;

public class MainApplication extends Application {
    private final AudioEngine engine = new VLCJAudioEngine();
    private final AudioPlayer player = new AudioPlayer(engine);
    private final MetaDataManager metaDataManger = new JaudiotaggerManager();
    private final MediaScanner scanner = new MediaScanner(metaDataManger);
    private final MediaLibrary library = new MediaLibrary();
    private final LibraryService libraryService = new LibraryService();
    private final MediaService mediaService = new MediaService(scanner,library,libraryService);
    private final PlayerService playerService = new PlayerService(player);
    private final int startingVolume = 50;
    private int oldVolume = startingVolume;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/mainView.fxml"));
        Parent root = loader.load();
        MainViewController controller = loader.getController();

        controller.setPlayer(player);
        controller.setMediaService(mediaService);
        controller.setLibraryService(libraryService);
        controller.setPlayerService(playerService);

        player.setVolume(startingVolume);

        IO.println("ov" + oldVolume + "nv" + player.getVolume());

        Scene scene = new Scene(root,1000,750);

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()){
                case P ->{
                    if (playerService.getCurrentTrack() != null) {
                        playerService.playFromList(playerService.getCurrentTrack(), playerService.getCurrentList());
                    }
                }
                case U -> {
                    if (player.getState() == PlaybackState.PLAYING)
                        playerService.pause();
                    else playerService.resume();
                }
                case D -> playerService.playNext();
                case A -> playerService.playPrev();
                case W -> player.setVolume(player.getVolume() + 10);
                case S -> player.setVolume(player.getVolume() - 10);
                case M -> {
                    if (player.getVolume() != 0) {
                        oldVolume = player.getVolume();
                        player.setVolume(0);
                        IO.println("muted");
                        IO.println("ov" + oldVolume + "nv" + player.getVolume());
                    } else {
                        player.setVolume(oldVolume);
                        IO.println("unmuted");
                        IO.println("ov" + oldVolume + "nv" + player.getVolume());
                    }
                }
                case F -> {
                    if (playerService.getCurrentTrack() != null) {
                        playerService.getCurrentTrack()
                                .setFavorite(!playerService.getCurrentTrack()
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
