package gui.controllers;

import application.sevice.LibraryService;
import application.sevice.MediaService;
import application.sevice.PlayerService;
import domain.model.Playlist;
import domain.model.Track;
import infrastructure.audio.AudioPlayer;
import domain.audio.PlaybackState;
import domain.audio.RepeatMode;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.util.Duration;
import domain.library.Library;


import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainViewController {

    @FXML private Button btnTracks;
    @FXML private Button btnSongs ;
    @FXML private Button btnBooks ;
    @FXML private Button btnPodcasts ;
    @FXML private Button btnPlay ;
    @FXML private Button btnNext ;
    @FXML private Button btnPrev ;
    @FXML private Button btnPlaylist ;
    @FXML private Button btnCurrentTrack ;
    @FXML private Button btnQueue ;
    @FXML private Button btnArtists;
    @FXML private Button btnGenres;
    @FXML
    private Button btnAlbum;
    @FXML
    private Button btnFolders;
    @FXML private Button btnFastForward;
    @FXML private Button btnFastBackward;
    @FXML private Button btnFavorite;
    @FXML
    private Button btnShuffle;

    @FXML
    private MenuButton btnRepeatAndStop;

    @FXML
    private MenuItem miPlayOne;
    @FXML
    private MenuItem miLoopOne;
    @FXML
    private MenuItem miPlayQueue;
    @FXML
    private MenuItem miLoopQueue;

    @FXML private Label currentTrack;

    @FXML private Slider volumeSlider;
    @FXML private Slider progressSlider;

    @FXML private AnchorPane contentArea;

    private MediaService mediaService ;
    private AudioPlayer player;
    private LibraryService libraryService;
    private PlayerService playerService;

    private MediaListViewController controller;

    private final int skipSeconds = 10; //planing to have user change it from settings which is not yet implemented


    public void setPlayer(AudioPlayer player) {
        this.player = player;
        System.out.println(player.getRepeatMode());
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
        updatePlayButton();
        setupLabel();

    }

    public void setMediaService(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    public void setLibraryService(LibraryService libraryService) {
        this.libraryService = libraryService;
        initializeLibrary();
    }

    public void updatePlayButton() {
        playerService.playbackStateProperty().addListener((obs, oldState, newState) -> {
            switch (newState) {
                case PLAYING -> btnPlay.setText("⏸");
                case PAUSED, STOPPED -> btnPlay.setText("▶");
            }
        });
    }

    @FXML
    private void initialize() {

        setButtonsEnabled(true);

        Task<Void> task = getVoidTask();

        btnTracks.setOnAction(e ->
                switchMediaView(new ArrayList<>(mediaService.getTracks()), ViewMode.TRACKS));

        btnSongs.setOnAction(e ->
                switchMediaView(new ArrayList<>(mediaService.getSongs()), ViewMode.SONGS));

        btnBooks.setOnAction(e ->
                switchMediaView(new ArrayList<>(mediaService.getAudioBooks()), ViewMode.BOOKS));

        btnPodcasts.setOnAction(event ->
                switchMediaView(new ArrayList<>(mediaService.getPodcasts()), ViewMode.PODCASTS));

        btnPlaylist.setOnAction(event -> loadPlaylistView());

        btnArtists.setOnAction(event -> loadCategoryView());

        btnGenres.setOnAction(event -> loadCategoryView());

        btnAlbum.setOnAction(event -> loadCategoryView());

        btnCurrentTrack.setOnAction(event -> loadPlayingView());

        btnFolders.setOnAction(event -> loadFoldersView());

        btnPlay.setOnAction(event -> {
                    if (player.getState() == PlaybackState.STOPPED) {
                        playerService.playFromList(playerService.getCurrentTrack(), playerService.getCurrentList());
                    } else if (player.getState() == PlaybackState.PLAYING) {
                        playerService.pause();
                    } else if (player.getState() == PlaybackState.PAUSED) {
                        playerService.resume();
                    }
                }
        );

        btnNext.setOnAction(event -> playerService.playNext());

        btnPrev.setOnAction(event -> playerService.playPrev());

        btnShuffle.setOnAction(event -> playerService.shuffle());

        setUpVolumeSlider();

        setUpProgressSlider();

        btnFastForward.setOnAction(event -> player.skipForward(skipSeconds));

        btnFastBackward.setOnAction(event -> player.skipBackward(skipSeconds));

        btnFavorite.setOnAction(event -> playerService.getCurrentTrack()
                .setFavorite(!playerService
                        .getCurrentTrack()
                        .isFavorite()));

        miPlayOne.setOnAction(event -> {
            player.setRepeatMode(RepeatMode.PLAY_ONE);
            updateRepeatButton(RepeatMode.PLAY_ONE);
        });
        miLoopOne.setOnAction(event -> {
            player.setRepeatMode(RepeatMode.LOOP_CURRENT_ONE);
            updateRepeatButton(RepeatMode.LOOP_CURRENT_ONE);
        });
        miPlayQueue.setOnAction(event -> {
            player.setRepeatMode(RepeatMode.STOP_WHEN_QUEUE_END);
            updateRepeatButton(RepeatMode.STOP_WHEN_QUEUE_END);
        });
        miLoopQueue.setOnAction(event -> {
            player.setRepeatMode(RepeatMode.LOOP_CURRENT_QUEUE);
            updateRepeatButton(RepeatMode.LOOP_CURRENT_QUEUE);
        });


        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @NotNull
    private Task<Void> getVoidTask() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                initializeLibrary();
                mediaService.loadActiveLibrary();
                player.enqueueAll(mediaService.getTracks());
                return null;
            }
        };

        task.setOnSucceeded(e -> {

            setButtonsEnabled(false);

            loadMediaView(
                    new ArrayList<>(mediaService.getSongs()),
                    ViewMode.SONGS
            );
        });
        return task;
    }

    public void setupLabel() {
        currentTrack.textProperty().bind(
                Bindings.createStringBinding(() -> {
                            Track track = playerService.getCurrentTrack();
                    return track == null ? "---" : track.getTitle();
                        }, playerService.currentTrackProperty()
                )
        );
    }

    private void updateRepeatButton(RepeatMode mode) {
        btnRepeatAndStop.setText(mode.getTitle());
        System.out.println(mode.getTitle());
    }

    private void setButtonsEnabled(boolean enabled) {
        btnTracks.setDisable(enabled);
        btnSongs.setDisable(enabled);
        btnBooks.setDisable(enabled);
        btnPodcasts.setDisable(enabled);
        btnPlaylist.setDisable(enabled);
    }

    private void setUpVolumeSlider(){
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            player.setVolume((int)(newVal.doubleValue() * 100));
        });
    }

    private void setUpProgressSlider() {

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(200), e -> {
                    if (!progressSlider.isValueChanging()) {
                        if (player.getState() != PlaybackState.STOPPED) {
                            progressSlider.setValue(player.getProgress() * 100);
                        }
                    }
                })
        );

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (progressSlider.isValueChanging()) {
                player.setProgress(newVal.floatValue() / 100f);
            }
        });
    }

    private void switchMediaView(List<Track> tracks, ViewMode mode) {
        loadMediaView(tracks, mode);
        controller.setData(tracks);
        controller.setMode(mode);
    }

    private void switchCategoryView(List<Playlist> categoryList, ViewMode mode) {
        loadCategoryView();

    }

    private void loadMediaView(List<Track> tracks, ViewMode mode) {
        try {
            FXMLLoader loader = loadView("/views/mediaList-view.fxml");

            controller = loader.getController();
            controller.setPlayerService(playerService);

        } catch (IOException e) {
            System.err.println("CRITICAL: Could not find or load mediaList-view.fxml");
            e.printStackTrace();
        } catch (IllegalStateException e) {
            System.err.println("FXML Error: Check if fx:controller is set correctly in mediaList-view.fxml");
            e.printStackTrace();
        }
    }

    private void loadPlaylistView() {
        try {
            FXMLLoader loader = loadView("/views/playlistView.fxml");

            PlaylistViewController playlistController = loader.getController();

            if (player != null) {
                playlistController.setPlayer(player);
            }

        } catch (IOException e) {
            System.err.println("Failed to load playlistView.fxml");
            e.printStackTrace();
        } catch (IllegalStateException e) {
            System.err.println("FXML Error: Check if fx:controller is set correctly in playlistView.fxml");
            e.printStackTrace();
        }
    }

    private void loadCategoryView() {
        try {
            FXMLLoader loader = loadView("/views/category-view.fxml");

            CategoryViewController categoryViewController = loader.getController();

            if (player != null) {
                categoryViewController.setPlayer(player);
            }
        } catch (IOException e) {
            System.err.println("CRITICAL: Could not find or load category-view.fxml");
            e.printStackTrace();
        } catch (IllegalStateException e) {
            System.err.println("FXML Error: Check if fx:controller is set correctly in category-view.fxml");
            e.printStackTrace();
        }
    }

    private void loadPlayingView() {
        try {
            FXMLLoader loader = loadView("/views/playing-track-view.fxml");

            PlayingTrackViewController playingTrackViewController = loader.getController();
            playingTrackViewController.setPlayerService(playerService);

        } catch (IOException e) {
            System.err.println("CRITICAL: Could not find or load playing-track-view.fxml");
            e.printStackTrace();
        } catch (IllegalStateException e) {
            System.err.println("FXML Error: Check if fx:controller is set correctly in playing-track-view.fxml");
            e.printStackTrace();
        }
    }

    private void loadFoldersView() {
        try {
            FXMLLoader loader = loadView("/views/folders-view.fxml");

            FoldersViewController foldersViewController = loader.getController();
            foldersViewController.setLibraryService(libraryService);

        } catch (IOException e) {
            System.err.println("CRITICAL: Could not find or load folders-view.fxml");
            e.printStackTrace();
        } catch (IllegalStateException e) {
            System.err.println("FXML Error: Check if fx:controller is set correctly in folders-view.fxml");
            e.printStackTrace();
        }
    }

    private FXMLLoader loadView(String resource) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(resource)
        );

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

            if (pathResult.isEmpty()) {
                return;
            }

            TextInputDialog nameDialog =
                    new TextInputDialog();

            nameDialog.setTitle("Library Setup");
            nameDialog.setHeaderText(
                    "Enter Library Name"
            );

            Optional<String> nameResult =
                    nameDialog.showAndWait();

            if (nameResult.isEmpty()) {
                return;
            }

            Library library =
                    libraryService.createLibrary(
                            nameResult.get(),
                            Path.of(pathResult.get())
                    );

            libraryService.setActiveLibrary(library);
        }

        if (!libraryService.hasActiveLibrary()) {

            libraryService.setActiveLibrary(
                    libraryService.getLibraries()
                            .getFirst()
            );
        }

        mediaService.loadActiveLibrary();
    }

    private static Optional<String> getResult() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("No Libraries Found");
        alert.setHeaderText("No music libraries available");
        alert.setContentText("Please create your first library.");
        alert.showAndWait();

        TextInputDialog pathDialog = new TextInputDialog();
        pathDialog.setTitle("Library Setup");
        pathDialog.setHeaderText("Enter Music Folder Path");
        return pathDialog.showAndWait();
    }

}
