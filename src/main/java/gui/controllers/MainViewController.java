package gui.controllers;

import application.service.LibraryService;
import application.service.MediaService;
import application.service.PlayerService;
import config.AppConfig;
import domain.model.media.Playlist;
import domain.model.media.Track;
import gui.utils.DialogFactory;
import gui.main.AppContext;
import gui.utils.ViewLoader;
import infrastructure.audio.AudioPlayer;
import domain.audio.RepeatMode;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.util.Duration;
import domain.model.library.Library;

import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static domain.audio.PlaybackState.*;

public class MainViewController {

    private static final Logger logger = LoggerFactory.getLogger(MainViewController.class);

    @FXML
    private Button btnTracks, btnSongs, btnBooks, btnPodcasts;
    @FXML
    private Button btnPlay, btnNext, btnPrev, btnPlaylist;
    @FXML
    private Button btnCurrentTrack, btnQueue, btnArtists, btnGenres;
    @FXML
    private Button btnAlbum, btnFolders, btnFastForward, btnFastBackward;
    @FXML
    private Button btnFavorite, btnShuffle, btnLyrics;

    @FXML
    private MenuButton btnRepeatAndStop;
    @FXML
    private MenuItem miPlayOne, miLoopOne, miPlayQueue, miLoopQueue;

    @FXML private Label currentTrack;
    @FXML private Slider volumeSlider;
    @FXML private Slider progressSlider;
    @FXML private AnchorPane contentArea;

    private final ViewMode DEFAULT_STARTING_VIEW_MODE = ViewMode.TRACKS;

    private AppContext appContext;
    private ViewLoader viewLoader;

    private MediaListViewController mediaListViewController;

    private boolean seeking = false;
    private ViewMode currentViewMode = DEFAULT_STARTING_VIEW_MODE;
    private ViewMode oldViewMode = DEFAULT_STARTING_VIEW_MODE;

    private static int skipSeconds;

    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
        this.viewLoader = new ViewLoader(appContext);
        skipSeconds = appContext.config().getPreferredSkipSeconds();
        initializeLibrary();
        init();
    }

    public void setBtnFavoriteStyle(boolean favorite) {
        if (favorite) {
            btnFavorite.setStyle("-fx-text-fill: #ff0000; -fx-font-weight: bold;");
        } else {
            btnFavorite.setStyle("-fx-text-fill: #c89b6d; -fx-font-weight: normal;");
        }
    }

    public void handleRefresh() {
        Platform.runLater(() -> {
            switchViewMode(currentViewMode);
            appContext.mediaService().refreshActiveLibrary();
        });
    }

    public void handelSwitchingBack() {
        Platform.runLater(() -> switchViewMode(oldViewMode));
    }

    @FXML
    private void initialize() {
        setButtonsEnabled(true);
        setUpViewButtons();
    }

    private void init() {
        setupButtonsVisibility();
        updatePlayButton();
        updateFavoriteButton();
        updateShuffleButton();
        setupLabel();
        setUpVolumeSlider();
        setUpControlButtons();
        setUpMenuOptions();
        setUpProgressSlider();
        updateRepeatButton();
        Task<Void> task = getQueueLoadingTask();
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void setupButtonsVisibility() {
        AppConfig config = appContext.config();
        btnTracks.setVisible(config.isTracksBtnVisibility());
        btnSongs.setVisible(config.isSongsBtnVisibility());
        btnBooks.setVisible(config.isBooksBtnVisibility());
        btnPodcasts.setVisible(config.isPodcastsBtnVisibility());
        btnArtists.setVisible(config.isArtistsBtnVisibility());
        btnGenres.setVisible(config.isGenresBtnVisibility());
        btnAlbum.setVisible(config.isAlbumsBtnVisibility());
        btnPlaylist.setVisible(config.isPlaylistsBtnVisibility());
    }

    private void updatePlayButton() {
        appContext.playerService().playbackStateProperty().addListener((obs, oldState, newState) -> {
            switch (newState) {
                case PLAYING -> btnPlay.setText("⏸");
                case PAUSED, STOPPED -> btnPlay.setText("▶");
            }
        });
    }

    private void updateFavoriteButton() {
        appContext.playerService().currentTrackProperty().addListener((obs, oldTrack, newTrack) -> {
            if (newTrack != null) {
                setBtnFavoriteStyle(newTrack.isFavorite());
            } else {
                setBtnFavoriteStyle(false);
            }
        });
    }

    private void updateShuffleButton() {
        appContext.playerService().shuffleProperty().addListener((obs, oldShuffle, newShuffle) -> {
            if (newShuffle) {
                btnShuffle.setText("shuffled");
                btnShuffle.getStyleClass().add("activated-button");
            } else {
                btnShuffle.setText("unshuffled");
                btnShuffle.getStyleClass().remove("activated-button");
            }
        });
    }

    private void updateRepeatButton() {
        appContext.playerService().repeatProperty()
                .addListener((
                        obs,
                        oldRepeat,
                        newRepeat
                ) -> btnRepeatAndStop.setText(newRepeat.getText()));
    }

    private void setUpViewButtons() {
        btnTracks.setOnAction(event -> switchViewMode(ViewMode.TRACKS));
        btnSongs.setOnAction(event -> switchViewMode(ViewMode.SONGS));
        btnBooks.setOnAction(event -> switchViewMode(ViewMode.BOOKS));
        btnPodcasts.setOnAction(event -> switchViewMode(ViewMode.PODCASTS));
        btnPlaylist.setOnAction(event -> switchViewMode(ViewMode.PLAYLIST));
        btnArtists.setOnAction(event -> switchViewMode(ViewMode.ARTISTS));
        btnGenres.setOnAction(event -> switchViewMode(ViewMode.GENRE));
        btnAlbum.setOnAction(event -> switchViewMode(ViewMode.ALBUM));
        btnCurrentTrack.setOnAction(event -> switchViewMode(ViewMode.TRACK));
        btnFolders.setOnAction(event -> switchViewMode(ViewMode.FOLDERS));
        btnLyrics.setOnAction(event -> switchViewMode(ViewMode.LYRICS));
        btnQueue.setOnAction(event -> switchViewMode(ViewMode.QUEUE));
    }

    private void switchViewMode(ViewMode viewMode) {
        MediaService mediaService = appContext.mediaService();
        oldViewMode = currentViewMode;
        switch (viewMode) {
            case TRACKS -> switchMediaView(new ArrayList<>(mediaService.getTracks()), viewMode);
            case SONGS -> switchMediaView(new ArrayList<>(mediaService.getSongs()), viewMode);
            case BOOKS -> switchMediaView(new ArrayList<>(mediaService.getAudioBooks()), viewMode);
            case PODCASTS -> switchMediaView(new ArrayList<>(mediaService.getPodcasts()), viewMode);
            case ALBUM -> switchCategoryView(new ArrayList<>(mediaService.getAlbums()), viewMode);
            case GENRE -> switchCategoryView(new ArrayList<>(mediaService.getGenre()), viewMode);
            case ARTISTS -> switchCategoryView(new ArrayList<>(mediaService.getArtists()), viewMode);
            case PLAYLIST -> loadPlaylistView();
            case TRACK -> loadPlayingTrackView();
            case LYRICS -> loadLyricsView();
            case FOLDERS -> loadFoldersView();
            case QUEUE -> loadQueueView();
            case SETTINGS -> IO.println("Settings not implemented yet");
        }
    }

    private void setUpMenuOptions() {
        AudioPlayer player = appContext.player();
        btnRepeatAndStop.setText(player.getRepeatMode().getText());
        miPlayOne.setOnAction(e -> {
            player.setRepeatMode(RepeatMode.PLAY_ONE);
            updateRepeatButton(RepeatMode.PLAY_ONE);
        });
        miLoopOne.setOnAction(e -> {
            player.setRepeatMode(RepeatMode.LOOP_CURRENT_ONE);
            updateRepeatButton(RepeatMode.LOOP_CURRENT_ONE);
        });
        miPlayQueue.setOnAction(e -> {
            player.setRepeatMode(RepeatMode.STOP_WHEN_QUEUE_END);
            updateRepeatButton(RepeatMode.STOP_WHEN_QUEUE_END);
        });
        miLoopQueue.setOnAction(e -> {
            player.setRepeatMode(RepeatMode.LOOP_CURRENT_QUEUE);
            updateRepeatButton(RepeatMode.LOOP_CURRENT_QUEUE);
        });
    }

    private void setUpControlButtons() {
        PlayerService playerService = appContext.playerService();
        setBtnPlay();
        setBtnFavorite();
        btnNext.setOnAction(event -> playerService.playNext());
        btnPrev.setOnAction(event -> playerService.playPrev());
        btnShuffle.setOnAction(event -> playerService.shuffle());
        btnFastForward.setOnAction(event -> playerService.skipForward(skipSeconds));
        btnFastForward.setText(skipSeconds + " ⏩");
        btnFastBackward.setOnAction(event -> playerService.skipBackward(skipSeconds));
        btnFastBackward.setText("⏪ " + skipSeconds);
    }

    private void setBtnFavorite() {
        PlayerService playerService = appContext.playerService();
        btnFavorite.setOnAction(event -> {
            Track current = playerService.getCurrentTrack();
            if (current != null) {
                current.setFavorite(!current.isFavorite());
                setBtnFavoriteStyle(current.isFavorite());
            }
        });
    }

    private void setBtnPlay() {
        PlayerService playerService = appContext.playerService();
        AudioPlayer player = appContext.player();
        btnPlay.setOnAction(event -> {
            switch (player.getState()) {
                case STOPPED -> playerService.playSelectedTrack();
                case PLAYING -> playerService.pause();
                case PAUSED -> playerService.resume();
            }
        });
    }

    private Task<Void> getQueueLoadingTask() {
        AudioPlayer player = appContext.player();
        MediaService mediaService = appContext.mediaService();
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                player.enqueueAll(mediaService.getTracks());
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            setButtonsEnabled(false);
            loadMediaView(new ArrayList<>(mediaService.getTracks()));
        });
        return task;
    }

    public void setupLabel() {
        PlayerService playerService = appContext.playerService();
        currentTrack.textProperty().bind(
                Bindings.createStringBinding(() -> {
                    Track track = playerService.getCurrentTrack();
                    return track == null ? "---" : track.getTitle();
                }, playerService.currentTrackProperty())
        );
    }

    private void updateRepeatButton(RepeatMode mode) {
        btnRepeatAndStop.setText(mode.getText());
    }

    private void setButtonsEnabled(boolean disabled) {
        btnTracks.setDisable(disabled);
        btnSongs.setDisable(disabled);
        btnBooks.setDisable(disabled);
        btnPodcasts.setDisable(disabled);
        btnPlaylist.setDisable(disabled);
    }

    private void setUpVolumeSlider() {
        PlayerService playerService = appContext.playerService();
        volumeSlider.valueProperty().bindBidirectional(playerService.volumeProperty());
    }

    private void setUpProgressSlider() {
        AudioPlayer player = appContext.player();
        Timeline progressTimeline = new Timeline(
                new KeyFrame(Duration.millis(200), e -> {
                    if (!seeking && !progressSlider.isValueChanging()) {
                        if (player.getState() != STOPPED) {
                            progressSlider.setValue(player.getProgress() * 100);
                        }
                    }
                })
        );
        progressTimeline.setCycleCount(Animation.INDEFINITE);
        progressTimeline.play();

        progressSlider.setOnMousePressed(e -> seeking = true);

        progressSlider.setOnMouseReleased(e -> {
            double target = progressSlider.getValue();
            player.setProgress((float) (target / 100f));

            seeking = false;
        });
    }

    private void switchMediaView(List<Track> tracks, ViewMode mode) {
        loadMediaView(tracks);
        currentViewMode = mode;
        if (mediaListViewController != null) {
            mediaListViewController.setData(tracks);
            mediaListViewController.init();
        }
    }

    private void switchCategoryView(List<Playlist> categoryList, ViewMode mode) {
        loadCategoryView(categoryList, mode);
        currentViewMode = mode;
    }

    private void loadMediaView(List<Track> tracks) {
        try {
            FXMLLoader loader = loadView("/views/mediaList-view.fxml");
            mediaListViewController = loader.getController();
            mediaListViewController.setUIContext(appContext);
            mediaListViewController.setViewLoader(viewLoader);
            mediaListViewController.setOnSaveSuccessCallback(this::handleRefresh);
        } catch (IOException e) {
            logger.error("CRITICAL: Could not find or load mediaList-view.fxml", e);
        }
    }

    private void loadPlaylistView() {
        try {
            FXMLLoader loader = loadView("/views/playlist-view.fxml");
            PlaylistViewController playlistController = loader.getController();
            playlistController.setUIContext(appContext);
            playlistController.setViewLoader(viewLoader);

            playlistController.setOnSaveSuccess(this::handleRefresh);

            currentViewMode = ViewMode.PLAYLIST;
        } catch (IOException e) {
            logger.error("Failed to load playlist-view.fxml", e);
        }
    }

    private void loadCategoryView(List<Playlist> categoryList, ViewMode mode) {
        try {
            FXMLLoader loader = loadView("/views/category-view.fxml");
            CategoryViewController categoryViewController = loader.getController();
            categoryViewController.setUiContext(appContext);
            categoryViewController.setData(categoryList, mode);

        } catch (IOException e) {
            logger.error("CRITICAL: Could not find or load category-view.fxml", e);
        }
    }

    private void loadPlayingTrackView() {
        try {
            FXMLLoader loader = loadView("/views/playing-track-view.fxml");
            PlayingTrackViewController playingTrackViewController = loader.getController();
            playingTrackViewController.setUiContext(appContext);
            currentViewMode = ViewMode.TRACK;
        } catch (IOException e) {
            logger.error("CRITICAL: Could not find or load playing-track-view.fxml", e);
        }
    }

    private void loadFoldersView() {
        try {
            FXMLLoader loader = loadView("/views/folders-view.fxml");
            FoldersViewController foldersViewController = loader.getController();
            foldersViewController.setLibraryService(appContext.libraryService());
            foldersViewController.setMediaService(appContext.mediaService());
            currentViewMode = ViewMode.FOLDERS;
        } catch (IOException e) {
            logger.error("CRITICAL: Could not find or load folders-view.fxml", e);
        }
    }

    private void loadLyricsView() {
        try {
            FXMLLoader loader = loadView("/views/lyrics-view.fxml");
            LyricsViewController lyricsViewController = loader.getController();
            lyricsViewController.setTrack(appContext.playerService().getCurrentTrack());
            lyricsViewController.setMetadataManager(appContext.metadataManager());
            currentViewMode = ViewMode.LYRICS;
        } catch (IOException e) {
            logger.error("CRITICAL: Could not find or load lyrics-view.fxml", e);
        }
    }

    private void loadQueueView() {
        try {
            FXMLLoader loader = loadView("/views/queued-tracks-view.fxml");
            QueuedTracksViewController queuedTracksViewController = loader.getController();
            queuedTracksViewController.setUIContext(appContext);
            queuedTracksViewController.setViewLoader(viewLoader);
            queuedTracksViewController.setOnSaveSuccessCallback(this::handleRefresh);
        } catch (IOException e) {
            logger.error("CRITICAL: Could not load queued-tracks-view.fxml", e);
        }
    }

    private FXMLLoader loadView(String resource) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
        Parent view = loader.load();

        contentArea.getChildren().setAll(view);
        AnchorPane.setTopAnchor(view, 0.0);
        AnchorPane.setBottomAnchor(view, 0.0);
        AnchorPane.setLeftAnchor(view, 0.0);
        AnchorPane.setRightAnchor(view, 0.0);

        return loader;
    }

    private void initializeLibrary() {
        LibraryService libraryService = appContext.libraryService();
        if (!libraryService.hasLibraries()) {
            Optional<String> pathResult = getResult();
            if (pathResult.isEmpty()) return;

            TextInputDialog nameDialog = DialogFactory.textInputDialog(
                    "Library Setup",
                    "Enter Library Name"
            );

            Optional<String> nameResult = nameDialog.showAndWait();
            if (nameResult.isEmpty()) return;

            Library library = libraryService.createLibrary(
                    nameResult.get(),
                    Path.of(pathResult.get())
            );
            libraryService.setActiveLibrary(library);
        }

        if (!libraryService.hasActiveLibrary()) {
            libraryService.setActiveLibrary(libraryService.getLibraries().getFirst());
        }

        appContext.mediaService().loadActiveLibrary();
    }

    private static Optional<String> getResult() {
        Alert alert = DialogFactory.warnings(
                "No Libraries Found",
                "No media libraries available",
                "Please create your first library.");
        alert.showAndWait();

        TextInputDialog pathDialog = DialogFactory.textInputDialog(
                "Library Setup",
                "Enter the path of your media folder"
        );

        return pathDialog.showAndWait();
    }
}
