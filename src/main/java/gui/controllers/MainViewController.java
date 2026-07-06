package gui.controllers;

import application.service.AppState;
import application.service.LibraryService;
import application.service.MediaService;
import application.service.PlayerService;
import domain.model.library.MediaLibrary;
import domain.model.media.Playlist;
import domain.model.media.Track;
import gui.utils.DialogFactory;
import infrastructure.audio.AudioPlayer;
import domain.audio.RepeatMode;
import infrastructure.media.MetadataManager;

import infrastructure.storage.ArtworkStorage;
import infrastructure.storage.MetadataStorage;
import infrastructure.storage.TrackStorage;
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static domain.audio.PlaybackState.*;

public class MainViewController {

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
    private MediaService mediaService;
    private AudioPlayer player;
    private LibraryService libraryService;
    private PlayerService playerService;
    private MetadataManager metadataManager;
    private MediaLibrary mediaLibrary;
    private AppState appState;
    private ArtworkStorage artStorage;
    private MetadataStorage metadataStorage;
    private TrackStorage trackStorage;

    private MediaListViewController mediaListViewController;

    private boolean seeking = false;
    private ViewMode currentViewMode = DEFAULT_STARTING_VIEW_MODE;

    static int skipSeconds = 10;

    public void setArtworkStorage(ArtworkStorage artStorage) {
        this.artStorage = artStorage;
    }

    public void setMetadataStorage(MetadataStorage metadataStorage) {
        this.metadataStorage = metadataStorage;
    }

    public void setTrackStorage(TrackStorage trackStorage) {
        this.trackStorage = trackStorage;
    }

    public void setPlayer(AudioPlayer player) {
        this.player = player;
        if (player != null)
            btnRepeatAndStop.setText(player.getRepeatMode().getText());
    }

    public void setPlayerService(PlayerService playerService) {
        if (this.playerService != null) return;
        this.playerService = playerService;
        updatePlayButton();
        updateFavoriteButton();
        setupLabel();
        setUpVolumeSlider();
    }

    public void setMetadataManager(MetadataManager metadataManager) {
        this.metadataManager = metadataManager;
    }

    public void setMediaService(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    public void setLibraryService(LibraryService libraryService) {
        this.libraryService = libraryService;
        initializeLibrary();
    }

    public void setMediaLibrary(MediaLibrary mediaLibrary) {
        this.mediaLibrary = mediaLibrary;
    }

    public void setAppState(AppState appState) {
        this.appState = appState;
    }

    public void updatePlayButton() {
        playerService.playbackStateProperty().addListener((obs, oldState, newState) -> {
            switch (newState) {
                case PLAYING -> btnPlay.setText("⏸");
                case PAUSED, STOPPED -> btnPlay.setText("▶");
            }
        });
    }

    public void updateFavoriteButton() {
        playerService.currentTrackProperty().addListener((obs, oldTrack, newTrack) -> {
            if (newTrack != null) {
                setBtnFavoriteStyle(newTrack.isFavorite());
            } else {
                setBtnFavoriteStyle(false);
            }
        });
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

    @FXML
    private void initialize() {
        setButtonsEnabled(true);

        setUpViewButtons();
        setUpControlButtons();
        setUpMenuOptions();
        setUpProgressSlider();

        Task<Void> task = getQueueLoadingTask();
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
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
    }

    private void switchViewMode(ViewMode viewMode) {
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
            case SETTINGS -> IO.println("Settings not implemented yet");
        }
    }

    private void setUpMenuOptions() {
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
        setBtnPlay();
        setBtnFavorite();
        btnNext.setOnAction(event -> playerService.playNext());
        btnPrev.setOnAction(event -> playerService.playPrev());
        btnShuffle.setOnAction(event -> playerService.shuffle());
        btnFastForward.setOnAction(event -> player.skipForward(skipSeconds));
        btnFastForward.setText(skipSeconds + " ⏩");
        btnFastBackward.setOnAction(event -> player.skipBackward(skipSeconds));
        btnFastBackward.setText("⏪ " + skipSeconds);
    }

    private void setBtnFavorite() {
        btnFavorite.setOnAction(event -> {
            Track current = playerService.getCurrentTrack();
            if (current != null) {
                current.setFavorite(!current.isFavorite());
                setBtnFavoriteStyle(current.isFavorite());
            }
        });
    }

    private void setBtnPlay() {
        btnPlay.setOnAction(event -> {
            switch (player.getState()) {
                case STOPPED -> playerService.playSelectedTrack();
                case PLAYING -> playerService.pause();
                case PAUSED -> playerService.resume();
            }
        });
    }

    private Task<Void> getQueueLoadingTask() {
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
        volumeSlider.valueProperty().bindBidirectional(playerService.volumeProperty());
    }

    private void setUpProgressSlider() {
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
            mediaListViewController.inti();
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
            mediaListViewController.setPlayerService(playerService);
            mediaListViewController.setMetadataManager(metadataManager);
            mediaListViewController.setMetadataStorage(metadataStorage);
            mediaListViewController.setTrackStorage(trackStorage);
            mediaListViewController.setMediaService(mediaService);
            mediaListViewController.setOnSaveSuccessCallback(this::handleRefresh);
        } catch (IOException e) {
            System.err.println("CRITICAL: Could not find or load mediaList-view.fxml");
        }
    }

    private void loadPlaylistView() {
        try {
            FXMLLoader loader = loadView("/views/playlist-view.fxml");
            PlaylistViewController playlistController = loader.getController();
            playlistController.setMediaLibrary(mediaLibrary);
            playlistController.setPlayerService(playerService);
            playlistController.setTracksList(mediaService.getTracks());
            playlistController.setOnSaveSuccess(this::handleRefresh);
            playlistController.setAppState(appState);
            currentViewMode = ViewMode.PLAYLIST;
        } catch (IOException e) {
            System.err.println("Failed to load playlist-view.fxml");
        }
    }

    private void loadCategoryView(List<Playlist> categoryList, ViewMode mode) {
        try {
            FXMLLoader loader = loadView("/views/category-view.fxml");
            CategoryViewController categoryViewController = loader.getController();
            categoryViewController.setPlayerService(playerService);
            categoryViewController.setAppState(this.appState);
            categoryViewController.setData(categoryList, mode);

        } catch (IOException e) {
            System.err.println("CRITICAL: Could not find or load category-view.fxml");
            e.printStackTrace();
        }
    }

    private void loadPlayingTrackView() {
        try {
            FXMLLoader loader = loadView("/views/playing-track-view.fxml");
            PlayingTrackViewController playingTrackViewController = loader.getController();
            playingTrackViewController.initializeDependencies(
                    playerService,
                    artStorage,
                    metadataStorage,
                    metadataManager
            );
            currentViewMode = ViewMode.TRACK;
        } catch (IOException e) {
            System.err.println("CRITICAL: Could not find or load playing-track-view.fxml");
        }
    }

    private void loadFoldersView() {
        try {
            FXMLLoader loader = loadView("/views/folders-view.fxml");
            FoldersViewController foldersViewController = loader.getController();
            foldersViewController.setLibraryService(libraryService);
            foldersViewController.setMediaService(mediaService);
            currentViewMode = ViewMode.FOLDERS;
        } catch (IOException e) {
            System.err.println("CRITICAL: Could not find or load folders-view.fxml");
        }
    }

    private void loadLyricsView() {
        try {
            FXMLLoader loader = loadView("/views/lyrics-view.fxml");
            LyricsViewController lyricsViewController = loader.getController();
            lyricsViewController.setTrack(playerService.getCurrentTrack());
            lyricsViewController.setMetadataManager(metadataManager);
            currentViewMode = ViewMode.LYRICS;
        } catch (IOException e) {
            System.err.println("CRITICAL: Could not find or load lyrics-view.fxml");
            e.printStackTrace();
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
        if (!libraryService.hasLibraries()) {
            Optional<String> pathResult = getResult();
            if (pathResult.isEmpty()) return;

            TextInputDialog nameDialog = new TextInputDialog();
            nameDialog.setTitle("Library Setup");
            nameDialog.setHeaderText("Enter Library Name");

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

        mediaService.loadActiveLibrary();
    }

    private static Optional<String> getResult() {
        Alert alert = DialogFactory.warnings(
                "No Libraries Found",
                "No media libraries available",
                "Please create your first library.");
        alert.showAndWait();

        TextInputDialog pathDialog = new TextInputDialog();
        pathDialog.setTitle("Library Setup");
        pathDialog.setHeaderText("Enter the path of your media folder");
        return pathDialog.showAndWait();
    }
}