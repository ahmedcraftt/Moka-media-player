package gui.controllers;

import application.service.PlayerService;
import config.UIConfig;
import domain.model.media.Track;
import gui.utils.DialogFactory;
import gui.main.AppContext;

import infrastructure.storage.DatabaseManager;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.CacheHint;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PlayingTrackViewController {

    private static final Logger logger = LoggerFactory.getLogger(PlayingTrackViewController.class);

    @FXML
    private Label lblTitle;
    @FXML
    private Label lblArtist;
    @FXML
    private ImageView imgTrack;
    @FXML
    private StackPane spImageContainer;
    @FXML
    private Button btnChangeArtwork;

    private RotateTransition rotateTransition;
    private ScaleTransition scaleTransition;

    private AppContext appContext;
    private int rotationSpeed;

    private ChangeListener<Track> trackListener;
    private ChangeListener<Object> playbackStateListener;

    public void setUiContext(AppContext appContext) {
        this.appContext = appContext;
        init();
    }

    private void init() {
        UIConfig config = appContext.config().getUIConfig();
        imgTrack.setFitWidth(config.getArtworkImageWidth());
        imgTrack.setFitHeight(config.getArtworkImageHeight());

        rotationSpeed = config.getArtworkRotationSpeed();
        setupRotation();

        PlayerService playerService = appContext.playerService();

        trackListener = (obs, oldT, newT) -> updateUI(newT);
        playerService.currentTrackProperty().addListener(trackListener);

        if (playerService.getCurrentTrack() != null) {
            updateUI(playerService.getCurrentTrack());
        } else {
            updateUI(playerService.getSelectedTrack());
        }

        playbackStateListener = (obs, oldState, newState) -> handlePlaybackStateChange(newState);
        playerService.playbackStateProperty()
                .addListener(playbackStateListener);

        if (playerService.playbackStateProperty().getValue() != null) {
            handlePlaybackStateChange(playerService.playbackStateProperty().getValue());
        }
    }

    @FXML
    private void initialize() {
        spImageContainer.setCache(true);
        spImageContainer.setCacheHint(CacheHint.SPEED);

        imgTrack.setSmooth(true);
        imgTrack.setCache(true);

        makeCircular(spImageContainer);
        setupBreathingAnimation();

        btnChangeArtwork.setVisible(false);
    }

    @FXML
    private void handleChangeArtwork() {
        PlayerService playerService = appContext.playerService();
        Track currentTrack = playerService.getCurrentTrack();
        if (currentTrack == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select New Track Artwork");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(btnChangeArtwork.getScene().getWindow());

        if (selectedFile != null) {
            btnChangeArtwork.setDisable(true);

            CompletableFuture.runAsync(() -> {
                try {
                    byte[] rawBytes = Files.readAllBytes(selectedFile.toPath());
                    String brandNewDiskPath = appContext.artStorage().saveArtwork(rawBytes, selectedFile.getName());

                    if (brandNewDiskPath != null) {
                        currentTrack.getMetadata().setArtworkPath(brandNewDiskPath);
                        try (Connection connection = DatabaseManager.connect()) {
                            appContext.trackStorage().update(currentTrack, connection);
                            connection.commit();
                        } catch (SQLException e) {

                        }

                        logger.info("Successfully updated artwork for track '{}' to local path: {}",
                                currentTrack.getTitle(), brandNewDiskPath);

                        Platform.runLater(() -> {
                            updateUI(currentTrack);
                            btnChangeArtwork.setDisable(false);
                        });
                    } else {
                        logger.warn("Artwork processing failed. Storage returned null path for file target: {}",
                                selectedFile.getName());
                        Platform.runLater(() -> {
                            showErrorAlert("Storage Error", "Could not save the artwork image to disk storage.");
                            btnChangeArtwork.setDisable(false);
                        });
                    }
                } catch (IOException e) {
                    logger.error("Exception encountered while reading or assigning new artwork file from input: {}",
                            selectedFile.getAbsolutePath(), e);
                    Platform.runLater(() -> {
                        showErrorAlert("File IO Error", "Failed to read selected file: " + e.getMessage());
                        btnChangeArtwork.setDisable(false);
                    });
                }
            });
        }
    }

    private void updateUI(Track track) {
        PlayerService playerService = appContext.playerService();
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> updateUI(track));
            return;
        }

        rotateTransition.stop();
        imgTrack.setRotate(0);

        if (playerService != null && playerService.isPlaying()) {
            rotateTransition.play();
        }

        if (track == null) {
            lblTitle.setText("No Track Playing");
            lblArtist.setText("Unknown Artist");
            imgTrack.setImage(null);
            btnChangeArtwork.setVisible(false);
            return;
        }

        btnChangeArtwork.setVisible(true);

        lblTitle.setText(track.getTitle());
        if (track.getMetadata().getArtist() != null) {
            lblArtist.setText(track.getMetadata().getArtist());
        } else {
            lblArtist.setText("Unknown Artist");
        }

        logger.debug("Refreshing track display interface layout target: {}", track);

        String artworkPath = track.getMetadata().getArtworkPath();

        if (artworkPath != null && !artworkPath.isBlank()) {
            File artworkFile = new File(artworkPath);
            if (artworkFile.exists()) {
                String imageFileUrl = artworkFile.toURI().toString();
                imgTrack.setImage(new Image(imageFileUrl, true));
            } else {
                logger.warn("Configured artwork path missing on disk space: {}. Applying UI fallbacks.", artworkPath);
                loadDefaultArtwork();
            }
        } else {
            loadDefaultArtwork();
        }
    }

    private void handlePlaybackStateChange(Object state) {
        if (state == null) return;

        String stateStr = state.toString();
        switch (stateStr) {
            case "PLAYING" -> {
                rotateTransition.play();
                scaleTransition.play();
            }
            case "PAUSED" -> {
                rotateTransition.pause();
                scaleTransition.pause();
            }
            case "STOPPED" -> {
                rotateTransition.stop();
                scaleTransition.stop();
                imgTrack.setRotate(0);
                imgTrack.setScaleX(1);
                imgTrack.setScaleY(1);
            }
        }
    }

    private void loadDefaultArtwork() {
        imgTrack.setImage(new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/assets/images/unknown.jpg")
                )));
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = DialogFactory.error(title, null, message);
        alert.showAndWait();
    }

    private void makeCircular(StackPane container) {
        Circle clip = new Circle();
        container.setClip(clip);

        container.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            double radius = Math.min(newVal.getWidth(), newVal.getHeight()) / 2;
            clip.setRadius(radius);
            clip.setCenterX(newVal.getWidth() / 2);
            clip.setCenterY(newVal.getHeight() / 2);
        });
    }

    private void setupRotation() {
        rotateTransition = new RotateTransition(Duration.seconds(rotationSpeed), spImageContainer);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
    }

    private void setupBreathingAnimation() {
        scaleTransition = new ScaleTransition(Duration.seconds(2), spImageContainer);
        scaleTransition.setFromX(1.0);
        scaleTransition.setToX(1.03);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToY(1.03);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(Animation.INDEFINITE);
        scaleTransition.setInterpolator(Interpolator.EASE_BOTH);
    }

    public void dispose() {
        if (appContext != null && appContext.playerService() != null) {
            PlayerService playerService = appContext.playerService();
            if (trackListener != null) {
                playerService.currentTrackProperty().removeListener(trackListener);
            }
            if (playbackStateListener != null) {
                playerService.playbackStateProperty()
                        .removeListener(playbackStateListener);
            }
        }
        if (rotateTransition != null) rotateTransition.stop();
        if (scaleTransition != null) scaleTransition.stop();
    }
}