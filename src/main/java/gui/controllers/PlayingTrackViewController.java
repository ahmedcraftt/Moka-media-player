
package gui.controllers;

import application.service.PlayerService;
import domain.model.media.Track;
import gui.utils.ImageConverter;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.CacheHint;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

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

    private RotateTransition rotateTransition;
    private ScaleTransition scaleTransition;

    private PlayerService playerService;

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
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
    }

    private void updateUI(Track track) {

        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> updateUI(track));
            return;
        }

        rotateTransition.stop();
        imgTrack.setRotate(0);

        if (playerService.isPlaying()) {
            rotateTransition.play();
        }

        if (track == null) {
            lblTitle.setText("No Track Playing");
            lblArtist.setText("Unknown Artist");
            imgTrack.setImage(null);
            return;
        }

        lblTitle.setText(track.getTitle());
        if (track.getMediaMetadata().getArtist() != null) {
            lblArtist.setText(track.getMediaMetadata().getArtist());
        } else lblArtist.setText("Unknown Artist");

        System.out.println("showing:" + track);

        var artwork = track.getMetadata().getArtwork();

        if (track.getMetadata().getArtwork() != null) {
            imgTrack.setImage(ImageConverter.convertToImage(artwork));
        } else imgTrack.setImage(new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/assets/images/unknown.jpg")
                )));
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

        scaleTransition = new ScaleTransition(
                Duration.seconds(2),
                spImageContainer
        );

        scaleTransition.setFromX(1.0);
        scaleTransition.setToX(1.03);

        scaleTransition.setFromY(1.0);
        scaleTransition.setToY(1.03);

        scaleTransition.setAutoReverse(true);

        scaleTransition.setCycleCount(Animation.INDEFINITE);

        scaleTransition.setInterpolator(Interpolator.EASE_BOTH);
    }

}