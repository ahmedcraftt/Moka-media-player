package infrastructure.audio;

import entities.Track;

import java.nio.file.Path;
import java.util.List;

public class AudioPlayer {
    private Track currentTrack;
    private final PlaybackQueue queue = new PlaybackQueue();
    private final AudioEngine engine = new VLCJAudioEngine();
    private PlaybackState state = PlaybackState.STOPPED;
    private RepeatMode repeatMode = RepeatMode.STOP_WHEN_QUEUE_END;

    public void play(Track track){
        if (track == null) return;
        Path path = track.getFilePath();
        currentTrack = track;
        queue.setCurrentTrack(currentTrack);
        engine.play(path,this::playNext);
        state = PlaybackState.PLAYING;
        queue.printStatus();
    }

    public void playNext() {
        System.out.println("playing next");
        switch (repeatMode) {
            case LOOP_CURRENT_ONE -> {
                System.out.println("looping "+ currentTrack.getMetadata().getTitle());
                if (currentTrack != null) play(currentTrack);
            }

            case PLAY_ONE ->{
                System.out.println("Stoping");
                stop();
            }

            case STOP_WHEN_QUEUE_END -> {
                Track nextTrack = queue.next();
                System.out.println("playing "+ nextTrack.getMetadata().getTitle());
                if (nextTrack != null) {
                    play(nextTrack);
                } else {
                    stop();
                }
            }

            case LOOP_CURRENT_QUEUE -> {
                Track nextTrack = queue.next();
                System.out.println("playing "+ nextTrack.getMetadata().getTitle());
                if (nextTrack == null) {
                    queue.reset();
                    nextTrack = queue.next();
                }

                if (nextTrack != null) {
                    play(nextTrack);
                } else {
                    stop(); // Only hits if the queue is physically empty
                }
            }
        }
    }

    public void playPrev(){
        if (engine.getProgress()==0f) {
            if (currentTrack != null) {
                Track track = queue.prev();
                play(track);
            }
        }else engine.setProgress(0f);
    }

    public void enqueueAll(List<Track> tracks) {
        if (tracks == null || tracks.isEmpty()) return;
        queue.addAll(tracks);
    }

    public void clearQueue(){
        queue.clear();
    }

    public void pause() {
        if (state == PlaybackState.PLAYING) {
            engine.pause();
            state = PlaybackState.PAUSED;
        }
    }

    public void stop() {
        if (state != PlaybackState.STOPPED) {
            engine.stop();
            currentTrack = null;
            state = PlaybackState.STOPPED;
        }
    }

    public void resume() {
        if (state == PlaybackState.PAUSED && currentTrack != null && !engine.isPlaying()) {
            state = PlaybackState.PLAYING;
            engine.resume();
        }
    }

    public void setVolume(int volume){
        if (volume<0) engine.setVolume(0);
        engine.setVolume(volume);
    }

    public void seek(float position){
        engine.seek(position);
    }

    public int getVolume (){
        return engine.getVolume();
    }

    public float getProgress(){
        return engine.getProgress();
    }

    public String getCurrentTimeStr(){
        return formatTime(engine.getCurrentTime());
    }

    public String getTotalTimeStr(){
        return formatTime(engine.getTotalTime());
    }

    public void skipForWard(int seconds){
        engine.skipForwards(seconds);
    }

    public void skipBackward(int seconds){
        engine.skipBackwards(seconds);
    }


    public RepeatMode getRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(RepeatMode mode) {
        this.repeatMode = mode;
        queue.setLoopQueue(mode == RepeatMode.LOOP_CURRENT_QUEUE);
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

    public void setShuffle(boolean enable) {
        queue.setShuffle(enable);
    }

    public boolean isShuffle() {
        return queue.isShuffleEnabled();
    }

    private String formatTime(long ms) {
        long seconds = ms / 1000;
        long mins = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }
}
