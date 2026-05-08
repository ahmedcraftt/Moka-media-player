
package ui.controllers;

import application.PlayerService;
import entities.Track;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class PlayingTrackViewController {

    @FXML
    private Label lblTitle;
    @FXML
    private Label lblArtist;
    @FXML
    private ImageView imgTrack;

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

        makeCircular(imgTrack);

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

        lblTitle.setText(track.getMetadata().getTitle());
        lblArtist.setText(track.getMetadata().getArtist());
        System.out.println("showing:" + track);

        var artwork = track.getMetadata().getArtwork();
        System.out.println(artwork == null);
        System.out.println(track.getMetadata().getArtwork().getClass());
        imgTrack.setImage(convertToImage(artwork));
    }

    private void makeCircular(ImageView imageView) {

        Circle clip = new Circle();

        imageView.setClip(clip);

        imageView.imageProperty().addListener((obs, oldImg, newImg) -> {

            if (newImg != null) {

                double radius = Math.min(
                        imageView.getFitWidth(),
                        imageView.getFitHeight()
                ) / 2;

                clip.setRadius(radius);

                clip.setCenterX(imageView.getFitWidth() / 2);
                clip.setCenterY(imageView.getFitHeight() / 2);
            }
        });
    }

    private void setupRotation() {
        rotateTransition = new RotateTransition(Duration.seconds(32), imgTrack);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
    }

    private void setupBreathingAnimation() {

        scaleTransition = new ScaleTransition(
                Duration.seconds(2),
                imgTrack
        );

        scaleTransition.setFromX(1.0);
        scaleTransition.setToX(1.03);

        scaleTransition.setFromY(1.0);
        scaleTransition.setToY(1.03);

        scaleTransition.setAutoReverse(true);

        scaleTransition.setCycleCount(Animation.INDEFINITE);

        scaleTransition.setInterpolator(Interpolator.EASE_BOTH);
    }

    private Image convertToImage(byte[] data) {
        if (data == null || data.length == 0) return null;

        return new Image(new java.io.ByteArrayInputStream(data));
    }
}