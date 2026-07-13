package gui.main;

import application.service.AppState;
import application.service.LibraryService;
import application.service.MediaService;
import application.service.PlayerService;
import domain.model.media.Track;
import gui.controllers.RefreshEvent;
import gui.utils.UIContext;
import infrastructure.audio.AudioEngine;
import infrastructure.audio.AudioPlayer;
import domain.audio.PlaybackState;
import infrastructure.audio.VLCJAudioEngine;
import infrastructure.media.FiledataManager;
import infrastructure.media.JaudiotaggerManager;
import infrastructure.scanner.MediaScanner;
import infrastructure.media.MetadataManager;
import domain.model.library.MediaLibrary;
import gui.controllers.MainViewController;
import infrastructure.storage.ArtworkStorage;
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
 * <p>
 * Responsibilities:
 * - Initializes all core application services and infrastructure.
 * - Creates and wires together the dependency graph manually
 * (without a dependency injection framework).
 * - Loads the main JavaFX UI and injects required services into the
 * MainViewController.
 * - Configures global keyboard shortcuts and application state.
 * - Performs startup performance logging.
 * - Handles graceful shutdown by persisting track data and releasing
 * native audio resources.
 * <p>
 * Startup sequence:
 * 1. Initialize SQLite storage.
 * 2. Construct infrastructure and application services.
 * 3. Load the JavaFX scene.
 * 4. Inject dependencies into the controller.
 * 5. Show the main window.
 * 6. Fire an initial RefreshEvent to populate the UI.
 * <p>
 * Shutdown sequence:
 * 1. Save track state.
 * 2. Release VLCJ/native audio resources.
 * 3. Log shutdown timings.
 */

public class MainApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);

    private static final int STARTING_VOLUME = 50;

    private final List<Path> startupFiles = new ArrayList<>();
    private final List<Path> startupDirectories = new ArrayList<>();
    private final List<Track> startupTracks = new ArrayList<>();

    private final AudioEngine audioEngine = new VLCJAudioEngine();
    private final AudioPlayer audioPlayer = new AudioPlayer(audioEngine);
    private final MetadataManager metadataManager = new JaudiotaggerManager();
    private final FiledataManager filedataManager = new FiledataManager();
    private final ArtworkStorage artworkStorage = new ArtworkStorage();
    private final MetadataStorage metadataStorage = new MetadataStorage();
    private final TrackStorage trackStorage = new TrackStorage(metadataStorage);
    private final MediaScanner mediaScanner = new MediaScanner(
            metadataManager,
            filedataManager,
            trackStorage,
            metadataStorage,
            artworkStorage
    );
    private final MediaLibrary mediaLibrary = new MediaLibrary();
    private final LibraryService libraryService = new LibraryService();
    private final MediaService mediaService = new MediaService(
            mediaScanner,
            mediaLibrary,
            libraryService
    );
    private final PlayerService playerService = new PlayerService(audioPlayer);
    private final AppState appState = new AppState();

    private final UIContext uiContext = new UIContext(
            mediaService,
            audioPlayer,
            libraryService,
            playerService,
            metadataManager,
            mediaLibrary,
            appState,
            artworkStorage,
            metadataStorage,
            trackStorage,
            mediaScanner
    );

    private int oldVolume = STARTING_VOLUME;

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
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

        controller.setUIContext(uiContext);
        logger.debug("MainViewController Dependency injection took {} ms", (System.nanoTime() - t) / 1_000_000.0);

        audioPlayer.setVolume(STARTING_VOLUME);

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
                        startupTracks.addAll(mediaScanner.scan(startupFiles)))
                .thenRun(() -> Platform.runLater(() -> {
                    for (Track track : startupTracks) {
                        playSelectedTrack(track);
                    }
                }));
    }

    private void setupStartupDirectories() {
        if (startupDirectories.isEmpty()) return;
        CompletableFuture.runAsync(() -> {
                    for (Path startupDirectory : startupDirectories) {
                        startupTracks.addAll(mediaScanner.scan(startupDirectory));
                    }
                })
                .thenRun(() -> Platform.runLater(() -> {
                    for (Track track : startupTracks) {
                        playSelectedTrack(track);
                    }
                }));
    }

    private void playSelectedTrack(Track track) {
        playerService.setSelectTrack(track);
        playerService.playSelectedTrack();
        logger.debug("Playing starting track at{} :\n {}", track.getFilePath(), track);
    }


    private void setupKeyBindings(Parent root, MainViewController controller, Scene scene, Stage stage) {
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
                case E -> playerService.skipForward(10);
                case Q -> playerService.skipBackward(10);
                case W -> audioPlayer.setVolume(Math.min(100, audioPlayer.getVolume() + 10));
                case S -> audioPlayer.setVolume(Math.max(0, audioPlayer.getVolume() - 10));
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
        long start = System.nanoTime();
        long t = start;

        logger.info("Intercepted shutdown signal. Closing Moka Player...");

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
                if (Files.exists(path) && mediaScanner.isAudioFile(path)) {
                    startupFiles.add(path);
                } else if (Files.isDirectory(path)) {
                    startupDirectories.add(path);
                }
            }
        }
    }
}