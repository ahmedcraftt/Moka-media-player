package infrastructure.audio;

import domain.audio.PlaybackListener;
import domain.audio.PlaybackState;
import domain.audio.RepeatMode;
import domain.model.media.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AudioPlayer {
    private static final Logger logger = LoggerFactory.getLogger(AudioPlayer.class);

    private final List<PlaybackListener> listeners = new ArrayList<>();
    private final PlaybackQueue queue = new PlaybackQueue();
    private final AudioEngine engine;
    private Track currentTrack;
    private PlaybackState state = PlaybackState.STOPPED;
    private RepeatMode repeatMode = RepeatMode.STOP_WHEN_QUEUE_END;

    public AudioPlayer(AudioEngine engine) {
        this.engine = engine;
    }

    public void play(Track track) {
        if (track == null) return;
        Path trackPath = track.getFilePath();
        currentTrack = track;
        queue.setCurrentTrack(track);

        currentTrack.incrementTimesPlayed();
        notifyTrackChanged();

        engine.play(trackPath.toUri(), this::playNext);

        track.setLastPlayed(LocalDateTime.now());
        System.out.println("playing " + track.getTitle());
        System.out.println(System.identityHashCode(track));
        System.out.println("last-played " + track.getLastPlayedAsString());

        if (track.getMetadata().getDurationInSeconds() <= 0) {
            track.getMetadata().setDurationInSeconds(engine.getDuration());
        }

        state = PlaybackState.PLAYING;
        notifyPlaybackStateChanged();
    }

    public void playFromList(Track selected, List<Track> list) {
        if (selected == null || list == null || list.isEmpty()) return;

        logger.debug("Initializing playlist context. Size: {}", list.size());
        queue.setupNavigationContext(selected, list);

        play(selected);
        logger.info("Navigation Ready. Prev:[{}] Current:[{}] Next:[{}]", peekPrevious(), currentTrack, peekNext());
    }

    public void playNext() {
        logger.debug("playNext() triggered under mode: {}", repeatMode);

        switch (repeatMode) {
            case LOOP_CURRENT_ONE -> {
                if (currentTrack != null) {
                    play(currentTrack);
                }
            }
            case PLAY_ONE -> stop();
            case STOP_WHEN_QUEUE_END -> {
                Track nextTrack = queue.next();
                if (nextTrack != null) {
                    play(nextTrack);
                } else {
                    stop();
                }
            }
            case LOOP_CURRENT_QUEUE -> {
                Track nextTrack = queue.next();
                if (nextTrack == null) {
                    queue.reset();
                    nextTrack = queue.next();
                }

                if (nextTrack != null) {
                    currentTrack = nextTrack;
                    play(currentTrack);
                } else {
                    stop();
                }
            }
        }
    }

    public void playPrev() {
        if (currentTrack == null) return;

        if (engine.getCurrentTime() < 3000) {
            Track prevTrack = queue.prev();
            if (prevTrack != null) {
                play(prevTrack);
            } else {
                engine.setProgress(0.0);
            }
        } else {
            engine.setProgress(0.0);
        }
    }

    public void pause() {
        if (state == PlaybackState.PLAYING) {
            engine.pause();
            state = PlaybackState.PAUSED;
            notifyPlaybackStateChanged();
        }
    }

    public void stop() {
        if (state != PlaybackState.STOPPED) {
            engine.stop();
            currentTrack = null;
            queue.setCurrentTrack(null);
            state = PlaybackState.STOPPED;
            notifyPlaybackStateChanged();
        }
    }

    public void resume() {
        if (state == PlaybackState.PAUSED && currentTrack != null && !engine.isPlaying()) {
            state = PlaybackState.PLAYING;
            engine.resume();
            notifyPlaybackStateChanged();
        }
    }

    public void enqueue(Track track) {
        queue.add(track);
    }

    public void enqueueAll(List<Track> tracks) {
        queue.addAll(tracks);
    }

    public void clearQueue() {
        queue.clear();
    }

    public Track peekNext() {
        return queue.peekNext();
    }

    public Track peekPrevious() {
        return queue.peekPrev();
    }

    public void removeTrackFromQueue(Track track) {
        queue.remove(track);
    }

    public void setVolume(int volume) {
        int clamped = Math.clamp(volume, 0, 100);
        if (engine.getVolume() != clamped) {
            engine.setVolume(clamped);
            notifyVolumeChanged(clamped);
        }
    }

    public int getVolume() {
        return engine.getVolume();
    }

    public void setProgress(double position) {
        engine.setProgress(position);
    }

    public double getProgress() {
        return engine.getProgress();
    }

    public void skipForward(int seconds) {
        engine.skipForwards(seconds);
    }

    public void skipBackward(int seconds) {
        engine.skipBackwards(seconds);
    }

    public long getTotalTimeSeconds() {
        return engine.getTotalTime() / 1000;
    }

    public long getCurrentTimeSeconds() {
        return engine.getCurrentTime() / 1000;
    }

    public void addPlaybackListener(PlaybackListener listener) {
        listeners.add(listener);
    }

    public void removePlaybackListener(PlaybackListener listener) {
        listeners.remove(listener);
    }

    private void notifyTrackChanged() {
        listeners.forEach(l -> l.onTrackChanged(currentTrack));
    }

    private void notifyPlaybackStateChanged() {
        listeners.forEach(l -> l.onPlaybackStateChanged(state));
    }

    private void notifyVolumeChanged(int vol) {
        listeners.forEach(l -> l.onVolumeChanged(vol));
    }

    private void notifyShuffleChanged(boolean shuffle) {
        listeners.forEach(l -> l.onShuffleChanged(shuffle));
    }

    private void notifyRepeatChanged(RepeatMode repeat) {
        listeners.forEach(l -> l.onRepeatChanged(repeat));
    }

    public RepeatMode getRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(RepeatMode mode) {
        this.repeatMode = mode;
        queue.setLoopQueue(mode == RepeatMode.LOOP_CURRENT_QUEUE);
        notifyRepeatChanged(mode);
    }

    public PlaybackState getState() {
        return state;
    }

    public Track getCurrentTrack() {
        return currentTrack;
    }

    public PlaybackQueue getQueue() {
        return queue;
    }

    public List<Track> getQueuedTracks() {
        return Collections.unmodifiableList(queue.getQueuedTracks());
    }

    public int getNumberOfTracks() {
        return queue.getNumberOfTracks();
    }

    public void setShuffle(boolean enable) {
        queue.setShuffle(enable);
        notifyShuffleChanged(enable);
    }

    public boolean isShuffle() {
        return queue.isShuffleEnabled();
    }
}