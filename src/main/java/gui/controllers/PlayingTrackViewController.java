package gui.controllers;

import application.service.PlayerService;
import domain.model.media.Track;
import infrastructure.media.MetadataManager;
import infrastructure.storage.ArtworkStorage;
import infrastructure.storage.TrackStorage;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public class PlayingTrackViewController {

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

    private PlayerService playerService;
    private ArtworkStorage artworkStorage;
    private TrackStorage trackStorage;
    private MetadataManager metadataManager;

    public void initializeDependencies(PlayerService playerService,
                                       ArtworkStorage artworkStorage,
                                       TrackStorage trackStorage,
                                       MetadataManager metadataManager) {
        this.playerService = playerService;
        this.artworkStorage = artworkStorage;
        this.trackStorage = trackStorage;
        this.metadataManager = metadataManager;

        playerService.currentTrackProperty().addListener((obs, oldT, newT) -> updateUI(newT));
        updateUI(playerService.getCurrentTrack());

        playerService.playbackStateProperty().addListener((obs, oldState, newState) -> {
            switch (newState) {
                case PLAYING -> {
                    rotateTransition.play();
                    scaleTransition.play();
                }
                case PAUSED -> {
                    rotateTransition.pause();
                    scaleTransition.pause();
                }
                case STOPPED -> {
                    rotateTransition.stop();
                    scaleTransition.stop();
                    imgTrack.setRotate(0);
                    imgTrack.setScaleX(1);
                    imgTrack.setScaleY(1);
                }
            }
        });
    }

    @FXML
    private void initialize() {
        spImageContainer.setCache(true);
        spImageContainer.setCacheHint(CacheHint.SPEED);

        imgTrack.setSmooth(true);
        imgTrack.setCache(true);

        makeCircular(spImageContainer);
        setupRotation();
        setupBreathingAnimation();

        scaleTransition.play();

        btnChangeArtwork.setVisible(false);
    }


    @FXML
    private void handleChangeArtwork() {
        Track currentTrack = playerService.getCurrentTrack();
        if (currentTrack == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select New Album Artwork");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(btnChangeArtwork.getScene().getWindow());

        if (selectedFile != null) {
            try {
                byte[] rawBytes = Files.readAllBytes(selectedFile.toPath());

                String brandNewDiskPath = artworkStorage.saveArtwork(rawBytes, selectedFile.getName());

                if (brandNewDiskPath != null) {
                    currentTrack.getMetadata().setArtworkPath(brandNewDiskPath);

                    metadataManager.write(currentTrack);

                    trackStorage.update(currentTrack);

                    updateUI(currentTrack);
                } else {
                    showErrorAlert("Storage Error", "Could not save the artwork image to disk storage.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                showErrorAlert("File IO Error", "Failed to read selected file: " + e.getMessage());
            }
        }
    }

    private void updateUI(Track track) {
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

        System.out.println("showing:" + track);

        String artworkPath = track.getMetadata().getArtworkPath();

        if (artworkPath != null && !artworkPath.isBlank()) {
            File artworkFile = new File(artworkPath);
            if (artworkFile.exists()) {
                String imageFileUrl = artworkFile.toURI().toString();
                imgTrack.setImage(new Image(imageFileUrl, true));
            } else {
                loadDefaultArtwork();
            }
        } else {
            loadDefaultArtwork();
        }
    }

    private void loadDefaultArtwork() {
        imgTrack.setImage(new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/assets/images/unknown.jpg")
                )));
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
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
        rotateTransition = new RotateTransition(Duration.seconds(64), spImageContainer);
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
}