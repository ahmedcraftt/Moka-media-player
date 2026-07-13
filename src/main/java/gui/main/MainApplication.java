package gui.main;

import application.service.MediaService;
import application.service.PlayerService;
import config.AppConfig;
import config.storage.ConfigStorage;
import domain.model.media.Track;
import gui.controllers.RefreshEvent;
import infrastructure.audio.AudioEngine;
import infrastructure.audio.AudioPlayer;
import domain.audio.PlaybackState;
import gui.controllers.MainViewController;
import infrastructure.storage.MetadataStorage;
import infrastructure.storage.TrackStorage;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static domain.audio.RepeatMode.*;
import static gui.main.MainLauncher.*;

/**
 * Main JavaFX application entry point for Moka Player.
 *
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Bootstraps the application using {@link AppContext}.</li>
 *     <li>Initializes persistent storage and loads application configuration.</li>
 *     <li>Constructs the JavaFX scene graph and injects the application context
 *     into the {@code MainViewController}.</li>
 *     <li>Restores player preferences such as volume, shuffle, and repeat mode.</li>
 *     <li>Processes command-line audio files and directories supplied at startup.</li>
 *     <li>Registers global keyboard and mouse playback shortcuts.</li>
 *     <li>Logs startup and shutdown performance.</li>
 *     <li>Persists configuration and track state during shutdown.</li>
 * </ul>
 *
 * <p>
 * Startup sequence:
 * <ol>
 *     <li>Create the shared {@link AppContext}.</li>
 *     <li>Initialize track and metadata storage.</li>
 *     <li>Restore player configuration.</li>
 *     <li>Load the JavaFX UI.</li>
 *     <li>Inject the application context into the main controller.</li>
 *     <li>Display the primary stage.</li>
 *     <li>Fire an initial {@link RefreshEvent}.</li>
 *     <li>Scan and enqueue any audio files or directories passed on the command line.</li>
 * </ol>
 *
 * <p>
 * Shutdown sequence:
 * <ol>
 *     <li>Stop playback.</li>
 *     <li>Persist application configuration.</li>
 *     <li>Save all modified track metadata.</li>
 *     <li>Release native audio resources.</li>
 *     <li>Close all application windows.</li>
 * </ol>
 */

public final class MainApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);

    private final List<Path> startupFiles = new ArrayList<>();
    private final List<Path> startupDirectories = new ArrayList<>();
    private final List<Track> startupTracks = new ArrayList<>();

    private final AppContext appContext = new AppContext();

    private int oldVolume;

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        AppConfig config = appContext.config();
        TrackStorage trackStorage = appContext.trackStorage();
        MetadataStorage metadataStorage = appContext.metadataStorage();
        AudioPlayer audioPlayer = appContext.player();

        audioPlayer.setRepeatMode(config.getPrefferredRepeatMode());
        audioPlayer.setShuffle(config.isShuffle());
        audioPlayer.setVolume(config.getPreferredVolumeLevel());

        oldVolume = audioPlayer.getVolume();

        primaryStage = stage;

        logger.debug(getParameters().getRaw().toString());

        long t = System.nanoTime();

        trackStorage.initialize();
        logger.debug("Track storage subsystem initialized in {} ms", (System.nanoTime() - t) / 1_000_000.0);
        t = System.nanoTime();

        metadataStorage.initialize();
        logger.debug("Metadata storage subsystem initialized in {} ms", (System.nanoTime() - t) / 1_000_000.0);
        t = System.nanoTime();

        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/views/main-view.fxml"));
        Parent root = loader.load();
        MainViewController controller = loader.getController();
        logger.debug("Main view FXML scene resolution took {} ms", (System.nanoTime() - t) / 1_000_000.0);
        t = System.nanoTime();

        controller.setAppContext(appContext);
        logger.debug("MainViewController Dependency injection took {} ms", (System.nanoTime() - t) / 1_000_000.0);

        Image icon = new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/assets/icons/app-icon.png")
                )
        );

        Scene scene = new Scene(root, 1080, 750);
        setupKeyBindings(root, controller, scene, stage);

        stage.setTitle("Moka Player ☕");
        stage.getIcons().add(icon);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setFullScreenExitHint("");

        root.addEventHandler(RefreshEvent.REFRESH, event -> controller.handleRefresh());

        stage.show();

        root.fireEvent(new RefreshEvent());

        setupStartupFiles();

        setupStartupDirectories();

        logger.info("Start up tracks {}", startupTracks);

        long elapsed = System.nanoTime() - START_TIME;
        logger.info("Moka Player UI engine successfully built and loaded in {} seconds",
                String.format("%.3f", (float) elapsed / 1_000_000_000.0f));
    }

    private void setupStartupFiles() {
        if (startupFiles.isEmpty()) return;
        CompletableFuture
                .runAsync(() ->
                        startupTracks.addAll(appContext.mediaScanner().scan(startupFiles)))
                .thenRun(() -> Platform.runLater(() ->
                        enqueueTracks(startupTracks)));
    }

    private void setupStartupDirectories() {
        if (startupDirectories.isEmpty()) return;
        CompletableFuture.runAsync(() -> {
                    for (Path startupDirectory : startupDirectories) {
                        startupTracks.addAll(appContext.mediaScanner().scan(startupDirectory));
                    }
                })
                .thenRun(() -> Platform.runLater(() ->
                        enqueueTracks(startupTracks)));
    }

    private void enqueueTracks(List<Track> tracks) {
        PlayerService playerService = appContext.playerService();
        for (Track track : tracks) {
            playerService.setSelectTrack(track);
            playerService.playSelectedTrack();
            logger.debug("Playing starting track at{} :\n {}", track.getFilePath(), track);
        }

    }

    private void setupKeyBindings(Parent root, MainViewController controller, Scene scene, Stage stage) {
        PlayerService playerService = appContext.playerService();
        AudioPlayer audioPlayer = appContext.player();
        AppConfig config = appContext.config();
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case P -> playerService.playSelectedTrack();
                case U, SPACE -> {
                    if (audioPlayer.getState() == PlaybackState.PLAYING) {
                        playerService.pause();
                    } else {
                        playerService.resume();
                    }
                }
                case D -> playerService.playNext();
                case A -> playerService.playPrev();
                case E -> playerService.skipForward(config.getPreferredSkipSeconds());
                case Q -> playerService.skipBackward(config.getPreferredSkipSeconds());
                case W -> audioPlayer.setVolume(Math.min
                        (100, audioPlayer.getVolume() + config.getPreferredVolumeModifier()));
                case S -> audioPlayer.setVolume(Math.max
                        (0, audioPlayer.getVolume() - config.getPreferredVolumeModifier()));
                case M -> {
                    if (audioPlayer.getVolume() != 0) {
                        oldVolume = audioPlayer.getVolume();
                        audioPlayer.setVolume(0);
                    } else {
                        audioPlayer.setVolume(oldVolume);
                    }
                }
                case H -> playerService.shuffle();
                case R -> {
                    var nextMode = switch (audioPlayer.getRepeatMode()) {
                        case STOP_WHEN_QUEUE_END -> LOOP_CURRENT_QUEUE;
                        case LOOP_CURRENT_QUEUE -> LOOP_CURRENT_ONE;
                        case LOOP_CURRENT_ONE -> PLAY_ONE;
                        case PLAY_ONE -> STOP_WHEN_QUEUE_END;
                    };
                    audioPlayer.setRepeatMode(nextMode);
                    logger.debug("Repeat mode changed to: {}", nextMode);
                }
                case F -> {
                    if (playerService.getCurrentTrack() != null) {
                        boolean isFav = playerService.getCurrentTrack().isFavorite();
                        playerService.getCurrentTrack().setFavorite(!isFav);
                    }
                }
                case B -> controller.handelSwitchingBack();
                case F11 -> stage.setFullScreen(!stage.isFullScreen());
                case F5 -> root.fireEvent(new RefreshEvent());
            }
        });
        scene.setOnMousePressed(event -> {
            switch (event.getButton()) {
                case FORWARD -> playerService.playNext();
                case BACK -> playerService.playPrev();
                case MIDDLE -> {
                    if (audioPlayer.getState() == PlaybackState.PLAYING) {
                        playerService.pause();
                    } else {
                        playerService.resume();
                    }
                }
            }
        });
    }

    @Override
    public void stop() throws Exception {
        TrackStorage trackStorage = appContext.trackStorage();
        MediaService mediaService = appContext.mediaService();
        AudioEngine audioEngine = appContext.audioEngine();
        long start = System.nanoTime();
        long t = start;

        logger.info("Intercepted shutdown signal. Closing Moka Player...");

        appContext.player().stop();

        ConfigStorage.save(appContext.config());

        if (mediaService != null && trackStorage != null) {
            trackStorage.saveAll(mediaService.getTracks());
            if (!startupTracks.isEmpty()) trackStorage.saveAll(startupTracks);
        }
        logger.debug("TrackStorage snapshot write-back took {} ms", (System.nanoTime() - t) / 1_000_000.0);
        t = System.nanoTime();

        if (audioEngine != null) {
            audioEngine.release();
        }
        logger.debug("AudioEngine native platform drivers released in {} ms", (System.nanoTime() - t) / 1_000_000.0);
        t = System.nanoTime();

        for (Window window : Window.getWindows()) {
            if (window instanceof Stage stage) {
                stage.close();
            }
        }
        logger.debug("Closing all sub stages in {} ms", (System.nanoTime() - t) / 1_000_000.0);

        long elapsed = System.nanoTime() - start;
        logger.info("Total teardown context finalized in {} seconds. Application terminated.",
                String.format("%.3f", (float) elapsed / 1_000_000_000.0f));

        super.stop();
    }

    @Override
    public void init() {
        List<String> args = getParameters().getRaw();
        if (!args.isEmpty()) {
            for (String arg : args) {
                Path path = Path.of(arg);
                if (Files.exists(path) && appContext
                        .mediaScanner().isAudioFile(path)) {
                    startupFiles.add(path);
                } else if (Files.isDirectory(path)) {
                    startupDirectories.add(path);
                }
            }
        }
    }
}