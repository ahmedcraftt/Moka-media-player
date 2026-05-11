package application.sevice;

import domain.model.Track;
import infrastructure.audio.AudioPlayer;
import infrastructure.audio.PlaybackListener;
import infrastructure.audio.PlaybackState;
import javafx.application.Platform;
import javafx.beans.property.*;

import java.util.List;

public class PlayerService {

    private final AudioPlayer player;

    private final ObjectProperty<List<Track>> currentList =
            new SimpleObjectProperty<>();

    private final ObjectProperty<Track> currentTrack =
            new SimpleObjectProperty<>();

    private final ObjectProperty<PlaybackState> playbackState =
            new SimpleObjectProperty<>(PlaybackState.STOPPED);

    private final BooleanProperty playing =
            new SimpleBooleanProperty(false);

    public PlayerService(AudioPlayer player) {

        this.player = player;

        player.addPlaybackListener(new PlaybackListener() {

            @Override
            public void onTrackChanged(Track newTrack) {

                Platform.runLater(() ->
                        currentTrack.set(newTrack)
                );
            }

            @Override
            public void onPlaybackStateChanged(PlaybackState state) {

                Platform.runLater(() -> {

                    playbackState.set(state);

                    playing.set(state == PlaybackState.PLAYING);
                });
            }
        });
    }

    // =========================
    // Playback Controls
    // =========================

    public void playFromList(Track selected, List<Track> trackList) {
        player.playFromList(selected, trackList);
    }

    public void playNext() {
        player.playNext();
    }

    public void playPrev() {
        player.playPrev();
    }

    public void pause() {
        player.pause();
    }

    public void resume() {
        player.resume();
    }

    public void stop() {
        player.stop();
    }

    public void shuffle() {
        player.setShuffle(!player.isShuffle());
    }

    // =========================
    // Properties
    // =========================

    public ObjectProperty<Track> currentTrackProperty() {
        return currentTrack;
    }

    public ObjectProperty<PlaybackState> playbackStateProperty() {
        return playbackState;
    }

    public ObjectProperty<List<Track>> currentListProperty() {
        return currentList;
    }

    public BooleanProperty playingProperty() {
        return playing;
    }

    // =========================
    // Getters
    // =========================

    public Track getCurrentTrack() {
        return currentTrack.get();
    }

    public void setCurrentTrack(Track track) {
        currentTrack.set(track);
    }

    public List<Track> getCurrentList() {
        return currentList.get();
    }

    public void setCurrentList(List<Track> list) {
        currentList.set(list);
    }

    public PlaybackState getPlaybackState() {
        return playbackState.get();
    }

    public boolean isPlaying() {
        return playing.get();
    }
}
