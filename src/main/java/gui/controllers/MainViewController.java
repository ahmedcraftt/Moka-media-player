package gui.controllers;

import application.service.LibraryService;
import application.service.MediaService;
import application.service.PlayerService;
import config.PlayerConfig;
import config.UIConfig;
import domain.model.media.Playlist;
import domain.model.media.Track;
import gui.models.TabsLocation;
import gui.models.ViewMode;
import gui.utils.DialogFactory;
import gui.main.AppContext;
import gui.utils.ViewLoader;
import infrastructure.audio.AudioPlayer;
import domain.audio.RepeatMode;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
import java.util.*;

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
    private Button btnAlbum, btnFolders, btnFastForward, btnFastBackward, btnRepeat;
    @FXML
    private Button btnFavorite, btnShuffle, btnLyrics, btnSettings;
    @FXML
    private ContextMenu cxmRepeatMenu;
    @FXML
    private MenuItem miPlayOne, miLoopOne, miPlayQueue, miLoopQueue;
    @FXML
    private Label lblCurrentTrack, lblNextTrack;
    @FXML private Slider volumeSlider;
    @FXML private Slider progressSlider;
    @FXML
    private AnchorPane apCenterArea;
    @FXML
    private VBox vbLeft, vbRight, vbMedia, vbControls;
    @FXML
    private HBox hbTop, hbMedia;

    private AppContext appContext;
    private ViewLoader viewLoader;

    private MediaListViewController mediaListViewController;

    private boolean seeking = false;
    private ViewMode currentViewMode;
    private int skipSeconds;

    private final Deque<ViewMode> viewModeStack = new ArrayDeque<>();
    private final Deque<ViewMode> pastViewStack = new ArrayDeque<>();

    private final List<Node> children = new ArrayList<>();

    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
        this.viewLoader = new ViewLoader(appContext);
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
        Platform.runLater(() -> switchViewMode(currentViewMode));
    }

    public void handleUpdate() {
        PlayerConfig playerConfig = appContext.config().getPlayerConfig();
        UIConfig uiConfig = appContext.config().getUIConfig();
        Platform.runLater(() -> {
            btnFastForward.setText(playerConfig.getPreferredSkipSeconds() + " ⏩");
            btnFastBackward.setText("⏪ " + playerConfig.getPreferredSkipSeconds());
            setupButtonsVisibility();
            switchTabsLocation();
        });
    }

    public void switchViewMode(ViewMode viewMode) {
        MediaService mediaService = appContext.mediaService();
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
            case SETTINGS -> loadSettingsView();
        }
    }

    public void switchBack() {
        Platform.runLater(() -> {
            if (pastViewStack.isEmpty()) {
                return;
            }

            viewModeStack.push(currentViewMode);
            currentViewMode = pastViewStack.pop();

            switchViewMode(currentViewMode);
        });
    }

    public void switchNext() {
        Platform.runLater(() -> {
            if (viewModeStack.isEmpty()) {
                return;
            }

            pastViewStack.push(currentViewMode);
            currentViewMode = viewModeStack.pop();

            switchViewMode(currentViewMode);
        });
    }

    @FXML
    private void initialize() {
        setButtonsEnabled(true);
        setUpViewButtons();
        hbTop.managedProperty().bind(hbTop.visibleProperty());
        hbMedia.managedProperty().bind(hbMedia.visibleProperty());
        vbLeft.managedProperty().bind(vbLeft.visibleProperty());
        vbRight.managedProperty().bind(vbRight.visibleProperty());
        vbControls.managedProperty().bind(vbControls.visibleProperty());
        vbMedia.managedProperty().bind(vbMedia.visibleProperty());
        children.addAll(vbMedia.getChildren());
    }

    private void init() {
        skipSeconds = appContext.config().getPlayerConfig().getPreferredSkipSeconds();
        currentViewMode = appContext.config().getUIConfig().getStartingViewMode();
        switchTabsLocation();
        setupButtonsVisibility();
        updatePlayButton();
        updateFavoriteButton();
        updateShuffleButton();
        setupLabels();
        setUpVolumeSlider();
        setUpControlButtons();
        setUpMenuOptions();
        setUpProgressSlider();
        updateRepeatButton();
        initializeLibrary();
        Task<Void> task = getQueueLoadingTask();
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void switchTabsLocation() {
        TabsLocation location = appContext.config().getUIConfig().getPreferredTabsLocation();
        hbMedia.getChildren().clear();
        vbMedia.getChildren().clear();
        switch (location) {
            case TOP -> {
                hbMedia.getChildren().addAll(children);
                hbMedia.setVisible(true);
                hbTop.setVisible(true);
                vbMedia.setVisible(false);
                vbLeft.setVisible(false);
            }

            case LEFT -> {
                vbMedia.getChildren().addAll(children);
                vbMedia.setVisible(true);
                vbLeft.setVisible(true);
                hbMedia.setVisible(false);
                hbTop.setVisible(false);
            }

            default -> {
                throw new IllegalStateException("Unexpected value: " + location);
            }

        }
    }

    private void setupButtonsVisibility() {
        UIConfig config = appContext.config().getUIConfig();
        btnTracks.setVisible(config.isTracksBtnVisibility());
        btnSongs.setVisible(config.isSongsBtnVisibility());
        btnBooks.setVisible(config.isBooksBtnVisibility());
        btnPodcasts.setVisible(config.isPodcastsBtnVisibility());
        btnArtists.setVisible(config.isArtistsBtnVisibility());
        btnGenres.setVisible(config.isGenresBtnVisibility());
        btnAlbum.setVisible(config.isAlbumsBtnVisibility());
        btnPlaylist.setVisible(config.isPlaylistsBtnVisibility());
        btnTracks.managedProperty().bind(btnTracks.visibleProperty());
        btnSongs.managedProperty().bind(btnSongs.visibleProperty());
        btnBooks.managedProperty().bind(btnBooks.visibleProperty());
        btnPodcasts.managedProperty().bind(btnPodcasts.visibleProperty());
        btnArtists.managedProperty().bind(btnArtists.visibleProperty());
        btnGenres.managedProperty().bind(btnGenres.visibleProperty());
        btnAlbum.managedProperty().bind(btnAlbum.visibleProperty());
        btnPlaylist.managedProperty().bind(btnPlaylist.visibleProperty());
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
                ) -> btnRepeat.setText(newRepeat.getText()));
    }

    private void setUpViewButtons() {
        btnTracks.setOnAction(event -> loadView(ViewMode.TRACKS));
        btnSongs.setOnAction(event -> loadView(ViewMode.SONGS));
        btnBooks.setOnAction(event -> loadView(ViewMode.BOOKS));
        btnPodcasts.setOnAction(event -> loadView(ViewMode.PODCASTS));
        btnPlaylist.setOnAction(event -> loadView(ViewMode.PLAYLIST));
        btnArtists.setOnAction(event -> loadView(ViewMode.ARTISTS));
        btnGenres.setOnAction(event -> loadView(ViewMode.GENRE));
        btnAlbum.setOnAction(event -> loadView(ViewMode.ALBUM));
        btnCurrentTrack.setOnAction(event -> loadView(ViewMode.TRACK));
        btnFolders.setOnAction(event -> loadView(ViewMode.FOLDERS));
        btnLyrics.setOnAction(event -> loadView(ViewMode.LYRICS));
        btnQueue.setOnAction(event -> loadView(ViewMode.QUEUE));
        btnSettings.setOnAction(event -> loadView(ViewMode.SETTINGS));
    }

    private void loadView(ViewMode mode) {
        if (mode == currentViewMode) {
            return;
        }

        pastViewStack.push(currentViewMode);

        currentViewMode = mode;

        viewModeStack.clear();

        switchViewMode(mode);
    }

    private void setUpMenuOptions() {
        AudioPlayer player = appContext.player();
        btnRepeat.setText(player.getRepeatMode().getText());
        btnRepeat.setOnAction(event -> cxmRepeatMenu.show(btnRepeat, Side.BOTTOM, 0, 0));
        miPlayOne.setOnAction(e -> player.setRepeatMode(RepeatMode.PLAY_ONE));
        miLoopOne.setOnAction(e -> player.setRepeatMode(RepeatMode.LOOP_CURRENT_ONE));
        miPlayQueue.setOnAction(e -> player.setRepeatMode(RepeatMode.STOP_WHEN_QUEUE_END));
        miLoopQueue.setOnAction(e -> player.setRepeatMode(RepeatMode.LOOP_CURRENT_QUEUE));
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

    public void setupLabels() {
        PlayerService playerService = appContext.playerService();
        lblCurrentTrack.textProperty().bind(
                Bindings.createStringBinding(() -> {
                    Track track = playerService.getCurrentTrack();
                    return track == null ? "---" : track.getTitle();
                }, playerService.currentTrackProperty())
        );
        lblNextTrack.textProperty().bind(
                Bindings.createStringBinding(() -> {
                    Track track = playerService.nextTrackProperty().get();
                    return track == null ? ">> ---" : ">> " + track.getTitle();
                }, playerService.nextTrackProperty())
        );
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
            FXMLLoader loader = viewLoader.loadView("/views/mediaList-view.fxml", apCenterArea);
            mediaListViewController = loader.getController();
            mediaListViewController.setAppContext(appContext);
            mediaListViewController.setViewLoader(viewLoader);
            mediaListViewController.setOnSaveSuccessCallback(this::handleRefresh);
        } catch (IOException e) {
            logger.error("CRITICAL: Could not find or load mediaList-view.fxml", e);
        }
    }

    private void loadPlaylistView() {
        try {
            FXMLLoader loader = viewLoader.loadView("/views/playlist-view.fxml", apCenterArea);
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
            FXMLLoader loader = viewLoader.loadView("/views/category-view.fxml", apCenterArea);
            CategoryViewController categoryViewController = loader.getController();
            categoryViewController.setUiContext(appContext);
            categoryViewController.setData(categoryList, mode);

        } catch (IOException e) {
            logger.error("CRITICAL: Could not find or load category-view.fxml", e);
        }
    }

    private void loadPlayingTrackView() {
        try {
            FXMLLoader loader = viewLoader.loadView("/views/playing-track-view.fxml", apCenterArea);
            PlayingTrackViewController playingTrackViewController = loader.getController();
            playingTrackViewController.setUiContext(appContext);
            currentViewMode = ViewMode.TRACK;
        } catch (IOException e) {
            logger.error("CRITICAL: Could not find or load playing-track-view.fxml", e);
        }
    }

    private void loadFoldersView() {
        try {
            FXMLLoader loader = viewLoader.loadView("/views/folders-view.fxml", apCenterArea);
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
            PlayerService playerService = appContext.playerService();
            FXMLLoader loader = viewLoader.loadView("/views/lyrics-view.fxml", apCenterArea);
            LyricsViewController lyricsViewController = loader.getController();
            if (playerService.getCurrentTrack() != null) {
                lyricsViewController.setTrack(playerService.getCurrentTrack());
            } else lyricsViewController.setTrack(appContext.playerService().getSelectedTrack());
            lyricsViewController.setAppContext(appContext);
            currentViewMode = ViewMode.LYRICS;
        } catch (IOException e) {
            logger.error("CRITICAL: Could not find or load lyrics-view.fxml", e);
        }
    }

    private void loadQueueView() {
        try {
            FXMLLoader loader = viewLoader.loadView("/views/queued-tracks-view.fxml", apCenterArea);
            QueuedTracksViewController queuedTracksViewController = loader.getController();
            currentViewMode = ViewMode.QUEUE;
            queuedTracksViewController.setAppContext(appContext);
            queuedTracksViewController.setViewLoader(viewLoader);
            queuedTracksViewController.setOnSave(this::handleRefresh);
        } catch (IOException e) {
            logger.error("CRITICAL: Could not load queued-tracks-view.fxml", e);
        }
    }

    private void loadSettingsView() {
        try {
            FXMLLoader loader = viewLoader.loadView("/views/settings-view.fxml", apCenterArea);
            SettingsViewController settingsViewController = loader.getController();
            currentViewMode = ViewMode.SETTINGS;
            settingsViewController.setAppContext(appContext);
        } catch (IOException e) {
            logger.error("CRITICAL: Could not load settings-view.fxml", e);
        }
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
