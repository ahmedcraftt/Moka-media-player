package gui.utils;

import application.service.PlayerService;
import config.AppConfig;
import domain.audio.PlaybackState;
import gui.controllers.MainViewController;
import gui.controllers.events.RefreshEvent;
import gui.main.AppContext;
import infrastructure.audio.AudioPlayer;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import static domain.audio.RepeatMode.*;
import static domain.audio.RepeatMode.STOP_WHEN_QUEUE_END;

public final class KeyAssignmentHandler {

    private static int oldVolume;

    public KeyAssignmentHandler() {
    }

    public static void setupKeyBindings(
            AppContext appContext,
            Parent root,
            MainViewController controller,
            Scene scene,
            Stage stage
    ) {
        PlayerService playerService = appContext.playerService();
        AudioPlayer audioPlayer = appContext.player();
        AppConfig config = appContext.config();
        oldVolume = audioPlayer.getVolume();
        keyboardBindings(appContext, scene, controller, stage, root);
        specialKeyBindings(appContext, scene);
        mouseKeyBindings(appContext, controller, scene);
    }

    private static void keyboardBindings(
            AppContext appContext,
            Scene scene,
            MainViewController controller,
            Stage stage,
            Parent root
    ) {
        PlayerService playerService = appContext.playerService();
        AudioPlayer audioPlayer = appContext.player();
        AppConfig config = appContext.config();
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case P -> playerService.playSelectedTrack();
                case U -> togglePlayPause(appContext);
                case O -> playerService.stop();
                case D -> playerService.playNext();
                case A -> playerService.playPrev();
                case E -> playerService.skipForward(config.getPlayerConfig().getPreferredSkipSeconds());
                case Q -> playerService.skipBackward(config.getPlayerConfig().getPreferredSkipSeconds());
                case W -> volumeUp(appContext);
                case S -> volumeDown(appContext);
                case M -> toggleMute(appContext);
                case H -> playerService.shuffle();
                case R -> {
                    var nextMode = switch (audioPlayer.getRepeatMode()) {
                        case STOP_WHEN_QUEUE_END -> LOOP_CURRENT_QUEUE;
                        case LOOP_CURRENT_QUEUE -> LOOP_CURRENT_ONE;
                        case LOOP_CURRENT_ONE -> PLAY_ONE;
                        case PLAY_ONE -> STOP_WHEN_QUEUE_END;
                    };
                    audioPlayer.setRepeatMode(nextMode);
                }
                case F -> {
                    if (playerService.getCurrentTrack() != null) {
                        boolean isFav = playerService.getCurrentTrack().isFavorite();
                        playerService.getCurrentTrack().setFavorite(!isFav);
                    }
                }
                case B -> controller.switchBack();
                case N -> controller.switchNext();
                case F11 -> stage.setFullScreen(!stage.isFullScreen());
                case F5 -> root.fireEvent(new RefreshEvent());
            }
        });
    }

    private static void specialKeyBindings(AppContext appContext, Scene scene) {
        PlayerService playerService = appContext.playerService();
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case SPACE -> {
                    togglePlayPause(appContext);
                    event.consume();
                }
                case UP -> {
                    volumeUp(appContext);
                    event.consume();
                }
                case DOWN -> {
                    volumeDown(appContext);
                    event.consume();
                }
                case LEFT -> {
                    playerService.playPrev();
                    event.consume();
                }
                case RIGHT -> {
                    playerService.playNext();
                    event.consume();
                }
            }
        });
    }

    private static void mouseKeyBindings(AppContext appContext, MainViewController controller, Scene scene) {
        scene.setOnMousePressed(event -> {
            switch (event.getButton()) {
                case FORWARD -> controller.switchNext();
                case BACK -> controller.switchBack();
                case MIDDLE -> togglePlayPause(appContext);
            }
        });
    }

    private static void volumeUp(AppContext appContext) {
        AudioPlayer audioPlayer = appContext.player();
        audioPlayer.setVolume(Math.min
                (100, audioPlayer.getVolume() +
                        appContext.config().getPlayerConfig().getPreferredVolumeModifier()));
    }

    private static void volumeDown(AppContext appContext) {
        AudioPlayer audioPlayer = appContext.player();
        audioPlayer.setVolume(Math.max
                (0, audioPlayer.getVolume() -
                        appContext.config().getPlayerConfig().getPreferredVolumeModifier()));
    }

    private static void togglePlayPause(AppContext appContext) {
        AudioPlayer audioPlayer = appContext.player();
        PlayerService playerService = appContext.playerService();
        if (audioPlayer.getState() == PlaybackState.PLAYING) {
            playerService.pause();
        } else {
            playerService.resume();
        }
    }

    private static void toggleMute(AppContext appContext) {
        AudioPlayer audioPlayer = appContext.player();
        if (audioPlayer.getVolume() != 0) {
            oldVolume = appContext.player().getVolume();
            appContext.player().setVolume(0);
        } else {
            appContext.player().setVolume(oldVolume);
        }
    }
}
