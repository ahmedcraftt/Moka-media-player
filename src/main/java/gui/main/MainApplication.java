package gui.main;

import application.sevice.LibraryService;
import application.sevice.MediaService;
import application.sevice.PlayerService;
import infrastructure.audio.AudioEngine;
import infrastructure.audio.AudioPlayer;
import domain.audio.PlaybackState;
import infrastructure.audio.VLCJAudioEngine;
import infrastructure.media.FiledataManager;
import infrastructure.media.JaudiotaggerManager;
import infrastructure.scanner.MediaScanner;
import infrastructure.media.MetadataManager;
import domain.library.MediaLibrary;
import gui.controllers.MainViewController;

import infrastructure.storage.TrackStorage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class MainApplication extends Application {

    private final AudioEngine engine = new VLCJAudioEngine();
    private final AudioPlayer player = new AudioPlayer(engine);
    private final MetadataManager metaDataManger = new JaudiotaggerManager();
    private final FiledataManager filedataManager = new FiledataManager();
    private final TrackStorage trackStorage = new TrackStorage();
    private final MediaScanner scanner = new MediaScanner(metaDataManger, filedataManager, trackStorage);
    private final MediaLibrary library = new MediaLibrary();
    private final LibraryService libraryService = new LibraryService();
    private final MediaService mediaService = new MediaService(scanner,library,libraryService);
    private final PlayerService playerService = new PlayerService(player);
    private final int startingVolume = 50;
    private int oldVolume = startingVolume;

    @Override
    public void start(Stage stage) throws IOException {
        trackStorage.initialize();

        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/views/main-view.fxml"));
        Parent root = loader.load();
        MainViewController controller = loader.getController();

        controller.setPlayer(player);
        controller.setMediaService(mediaService);
        controller.setLibraryService(libraryService);
        controller.setPlayerService(playerService);
        controller.setMetadataManager(metaDataManger);
        controller.setMediaLibrary(library);



        player.setVolume(startingVolume);

        Image icon = new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/assets/icons/app-icon.png")
                )
        );

        Scene scene = new Scene(root,1000,750);

        setupKeyBindings(scene, stage);

        stage.setTitle("Moka Player ☕");
        stage.getIcons().add(icon);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setOnCloseRequest(
                event -> {
                    System.out.println("Closing app...");
                    trackStorage.saveAll(mediaService.getTracks());
                    engine.release();
                    event.consume();
                    Platform.exit();
                });
        stage.show();

        stage.setFullScreenExitHint("");

    }

    private void setupKeyBindings(Scene scene, Stage stage) {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()){
                case P -> playerService.playSelectedTrack();
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
                    } else {
                        player.setVolume(oldVolume);
                    }
                }
                case F -> {
                    if (playerService.getCurrentTrack() != null) {
                        playerService.getCurrentTrack()
                                .setFavorite(!playerService.getCurrentTrack()
                                        .isFavorite());
                    }
                }
                case F11 -> stage.setFullScreen(!stage.isFullScreen());
            }
        });
    }
}
