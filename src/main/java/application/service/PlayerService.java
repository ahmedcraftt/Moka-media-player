package application.service;

import domain.model.media.Track;
import infrastructure.audio.AudioPlayer;
import domain.audio.PlaybackListener;
import domain.audio.PlaybackState;
import javafx.application.Platform;
import javafx.beans.property.*;

import java.util.List;

public class PlayerService {

    private final AudioPlayer player;

    private final ObjectProperty<List<Track>> currentList =
            new SimpleObjectProperty<>();

    private final ObjectProperty<Track> selectedTrack =
            new SimpleObjectProperty<>();

    private final ObjectProperty<Track> currentTrack =
            new SimpleObjectProperty<>();

    private final ObjectProperty<PlaybackState> playbackState =
            new SimpleObjectProperty<>(PlaybackState.STOPPED);

    private final BooleanProperty playing =
            new SimpleBooleanProperty(false);

    private final DoubleProperty volume =
            new SimpleDoubleProperty(0.5);// Default to 50%

    private final BooleanProperty shuffle =
            new SimpleBooleanProperty(false);

    public PlayerService(AudioPlayer player) {
        this.player = player;

        this.volume.set(player.getVolume() / 100.0);

        this.shuffle.set(player.isShuffle());

        this.volume.addListener((obs, oldVal, newVal) -> {
            int targetVol = (int) Math.round(newVal.doubleValue() * 100);
            if (player.getVolume() != targetVol) {
                player.setVolume(targetVol);
            }
        });

        player.addPlaybackListener(new PlaybackListener() {

            @Override
            public void onTrackChanged(Track newTrack) {
                Platform.runLater(() -> currentTrack.set(newTrack));
            }

            @Override
            public void onPlaybackStateChanged(PlaybackState state) {
                Platform.runLater(() -> {
                    playbackState.set(state);
                    playing.set(state == PlaybackState.PLAYING);
                });
            }

            @Override
            public void onVolumeChanged(int newVolume) {
                Platform.runLater(() -> {
                    double targetSliderValue = newVolume / 100.0;
                    if (Math.abs(volume.get() - targetSliderValue) > 0.01) {
                        volume.set(targetSliderValue);
                    }
                });
            }

            @Override
            public void onShuffleChanged(boolean newShuffle) {
                Platform.runLater(() -> shuffle.set(newShuffle));
            }
        });
    }

    // =========================
    // Playback Controls
    // =========================

    public void playSelectedTrack() {
        Track selected = selectedTrack.get();
        if (selected == null || currentList.get() == null) {
            return;
        }
        playFromList(selected, currentList.get());
    }

    public void playFromList(Track selected, List<Track> trackList) {
        if (!trackList.contains(selected)) {
            trackList.add(0, selected);
        }
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

    public void removeTrackFromQueue(Track track) {
        player.removeTrackFromQueue(track);
    }

    public void skipForward(int i) {
        player.skipForward(i);
    }

    public void skipBackward(int i) {
        player.skipBackward(i);
    }
    // =========================
    // Properties
    // =========================

    public DoubleProperty volumeProperty() {
        return volume;
    }

    public ObjectProperty<Track> currentTrackProperty() {
        return currentTrack;
    }

    public ObjectProperty<PlaybackState> playbackStateProperty() {
        return playbackState;
    }

    public ObjectProperty<List<Track>> currentListProperty() {
        return currentList;
    }

    public ObjectProperty<Track> selectedTrackProperty() {
        return selectedTrack;
    }

    public BooleanProperty playingProperty() {
        return playing;
    }

    public BooleanProperty shuffleProperty() {
        return shuffle;
    }

    // =========================
    // Getters and setters
    // =========================

    public double getVolume() {
        return volume.get();
    }

    public void setVolume(double vol) {
        this.volume.set(vol);
    }

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

    public Track getSelectedTrack() {
        return selectedTrack.get();
    }

    public void setSelectTrack(Track track) {
        selectedTrack.set(track);
    }

    public boolean isPlaying() {
        return playing.get();
    }

}