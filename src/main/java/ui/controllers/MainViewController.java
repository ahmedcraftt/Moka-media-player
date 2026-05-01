package ui.controllers;

import application.LibraryService;
import application.MediaService;
import entities.Track;
import infrastructure.audio.AudioPlayer;
import infrastructure.audio.PlaybackState;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import mediaLibrary.Library;


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
    @FXML private Button btnRepeatAndStop ;
    @FXML private Button btnArtists;
    @FXML private Button btnGenres;
    @FXML private Button btnFastForward;
    @FXML private Button btnFastBackward;
    @FXML private Button btnFavorite;

    @FXML private Label currentTrack;

    @FXML private Slider volumeSlider;
    @FXML private Slider progressSlider;

    @FXML private AnchorPane contentArea;

    private MediaService mediaService ;
    private AudioPlayer player;
    private ControllerServer controllerServer;
    private LibraryService libraryService;

    private MediaListViewController controller;

    private Timeline timeline;
    private int volume ; //not yet used
    private int skipSeconds = 10; //planing to have user change it from settings which is not yet implemented

    public void setPlayerService(ControllerServer controllerServer) {
        this.controllerServer = controllerServer;
    }

    public void setPlayer(AudioPlayer player) {
        this.player = player;
        volume = player.getVolume();
        System.out.println(player.getRepeatMode());
    }

    public void setMediaService(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    public void setLibraryService(LibraryService libraryService) {
        this.libraryService = libraryService;
        initializeLibrary();
    }

    @FXML
    private void initialize() {

        setButtonsEnabled(true);

        Task<Void> task = getVoidTask();

        btnTracks.setOnAction(e ->
                switchView(new ArrayList<>(mediaService.getTracks()),ViewMode.TRACKS));

        btnSongs.setOnAction(e ->
                switchView(new ArrayList<>(mediaService.getSongs()), ViewMode.SONGS));

        btnBooks.setOnAction(e ->
                switchView(new ArrayList<>(mediaService.getAudioBooks()), ViewMode.BOOKS));

        btnPodcasts.setOnAction(e ->
                switchView(new ArrayList<>(mediaService.getPodcasts()), ViewMode.PODCASTS));

        btnPlaylist.setOnAction(e -> loadPlaylistView());

        btnArtists.setOnAction(event -> loadCategoryView());

        btnGenres.setOnAction(event -> loadCategoryView());

        btnPlay.setOnAction(event -> {
                    if (player.getState()==PlaybackState.STOPPED) {
                        Track track = controllerServer.getSelectedTrack();
                        player.play(track);
                        setupLabel(track);
                        updatePlayButton();
                    }else if (player.getState()== PlaybackState.PLAYING){
                        player.pause();
                        updatePlayButton();
                    }else if (player.getState() == PlaybackState.PAUSED){
                        player.resume();
                        updatePlayButton();
                    }
                }
        );

        btnNext.setOnAction(event -> player.playNext());

        btnPrev.setOnAction(event -> player.playPrev());

        setUpVolumeSlider();

        setUpProgressSlider();

        btnFastForward.setOnAction(event -> player.skipForWard(skipSeconds));

        btnFastBackward.setOnAction(event -> player.skipBackward(skipSeconds));

        btnFavorite.setOnAction(event -> {
            if (!controllerServer.getSelectedTrack().isFavorite()){
                controllerServer.getSelectedTrack().setFavorite(true);
                btnFavorite.setText("♥\uFE0F");
            }else {
                controllerServer.getSelectedTrack().setFavorite(false);
                btnFavorite.setText("♥");
            }
        });

        if (player!= null) setupLabel(player.getCurrentTrack());

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

    public void setupLabel(Track track){
        if (track != null){
            currentTrack.setText(track.getMetadata().getTitle());
        } else {
            currentTrack.setText("---");
        }
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

        timeline = new Timeline(
                new KeyFrame(Duration.millis(200), e -> {
                    if (!progressSlider.isValueChanging()) {
                        if (player.getState()!=PlaybackState.STOPPED) {
                            progressSlider.setValue(player.getProgress() * 100);
                        }
                    }
                })
        );

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (progressSlider.isValueChanging()) {
                player.seek(newVal.floatValue() / 100f);
            }
        });
    }

    private void switchView(List<Track> tracks, ViewMode mode) {
        loadMediaView(tracks, mode);
        controller.setData(tracks);
        controller.setMode(mode);
    }

    private void loadMediaView(List<Track> tracks, ViewMode mode) {
        try {
            FXMLLoader loader = loadView("/mediaListView.fxml");

            controller = loader.getController();

            if (player != null) {
                controller.setPlayer(player);
            }
            if (controllerServer != null){
                controller.setPlayerService(controllerServer);
            }

        } catch (IOException e) {
            System.err.println("CRITICAL: Could not find or load mediaListView.fxml");
            e.printStackTrace();
        } catch (IllegalStateException e) {
            System.err.println("FXML Error: Check if fx:controller is set correctly in mediaListView.fxml");
            e.printStackTrace();
        }
    }

    private void loadPlaylistView() {
        try {
            FXMLLoader loader = loadView("/playlistView.fxml");

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
            FXMLLoader loader = loadView("/categoryView.fxml");

            CategoryViewController categoryViewController = loader.getController();

            if (player != null) {
                categoryViewController.setPlayer(player);
            }
        } catch (IOException e) {
            System.err.println("CRITICAL: Could not find or load categoryView.fxml");
            e.printStackTrace();
        } catch (IllegalStateException e) {
            System.err.println("FXML Error: Check if fx:controller is set correctly in categoryView.fxml");
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

    private void updatePlayButton() {
        switch (player.getState()) {
            case PLAYING -> btnPlay.setText("⏸");
            case PAUSED, STOPPED -> btnPlay.setText("▶");
        }
    }

    private void initializeLibrary() {

        if (!libraryService.hasLibraries() || !libraryService.hasActiveLibrary()) {

            Optional<String> pathResult = getResult();

            if (pathResult.isEmpty()) return;

            TextInputDialog nameDialog = new TextInputDialog();
            nameDialog.setTitle("Library Setup");
            nameDialog.setHeaderText("Enter Library Name");
            Optional<String> nameResult = nameDialog.showAndWait();

            if (nameResult.isEmpty()) return;

            String path = pathResult.get();
            String name = nameResult.get();

            Library lib = new Library(name, Path.of(path), true);

            libraryService.addLibrary(lib);
            libraryService.setActiveLibrary(lib);

            mediaService.loadActiveLibrary();

            return;
        }

        if (!libraryService.hasActiveLibrary()) {
            libraryService.setActiveLibrary(
                    libraryService.getLibraries().getFirst()
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
