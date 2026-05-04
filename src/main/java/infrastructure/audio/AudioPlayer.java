package infrastructure.audio;

import entities.Track;

import java.nio.file.Path;
import java.util.List;

public class AudioPlayer {

    /**
     * Core audio playback controller responsible for managing playback state,
     * queue navigation, repeat/shuffle behavior, and delegating playback to the AudioEngine.
     *
     * <p>This class acts as the central coordinator between user actions (UI)
     * and the low-level AudioEngine. It does not process or decode audio itself.</p>
     *
     * <h2>Architecture Overview</h2>
     * <ul>
     *   <li><b>AudioPlayer</b> → Controls playback logic and state</li>
     *   <li><b>PlaybackQueue</b> → Manages track order, history, and navigation</li>
     *   <li><b>AudioEngine</b> → Handles actual audio playback</li>
     * </ul>
     *
     * <h2>Key Responsibilities</h2>
     * <ul>
     *   <li>Play, pause, resume, and stop audio</li>
     *   <li>Navigate tracks (next/previous)</li>
     *   <li>Manage playback queue and history</li>
     *   <li>Handle repeat modes and shuffle behavior</li>
     *   <li>Forward playback operations to AudioEngine</li>
     * </ul>
     *
     * <h2>Playback Flow</h2>
     * <pre>
     * UI → AudioPlayer → PlaybackQueue → AudioEngine → System Audio
     * </pre>
     *
     * <h2>Important Notes</h2>
     * <ul>
     *   <li>AudioEngine triggers {@link #playNext()} automatically when a track finishes.</li>
     *   <li>Queue maintains both forward tracks and history for navigation.</li>
     *   <li>{@link #playFromList(Track, List)} reconstructs queue context around a selected track.</li>
     * </ul>
     */

    private Track currentTrack;
    private final PlaybackQueue queue = new PlaybackQueue();
    private final AudioEngine engine;
    private PlaybackState state = PlaybackState.STOPPED;
    private RepeatMode repeatMode = RepeatMode.STOP_WHEN_QUEUE_END;

    public AudioPlayer(AudioEngine engine) {
        this.engine = engine;
    }

    public void printStatus() {
        System.out.println(this);
    }

    /**
     * Starts playback of a specific track.
     *
     * <p>This sets the current track, updates the queue pointer,
     * and delegates playback to the AudioEngine.</p>
     *
     * <p>When playback finishes, AudioEngine will trigger {@link #playNext()}.</p>
     *
     * @param track the track to play
     */
    public void play(Track track){
        if (track == null) return;
        Path path = track.getFilePath();
        currentTrack = track;
        queue.setCurrentTrack(currentTrack);
        engine.play(path,this::playNext);
        state = PlaybackState.PLAYING;
        printStatus();
    }

    /**
     * Starts playback from a selected track within a given list and rebuilds queue context.
     *
     * <p>Queue behavior:
     * <ul>
     *   <li>Tracks after the selected track are added to the forward queue</li>
     *   <li>Tracks before the selected track are stored as history</li>
     * </ul>
     *
     * <p>This enables proper next/previous navigation relative to the selected track.</p>
     *
     * @param selected the track to start playback from
     * @param list     the full ordered list of tracks
     */
    public void playFromList(Track selected, List<Track> list) {
        if (selected == null || list == null || list.isEmpty()) return;

        queue.clear();

        int index = list.indexOf(selected);
        if (index == -1) return;

        queue.setCurrentTrack(selected);

        for (int i = index + 1; i < list.size(); i++) {
            queue.add(list.get(i));
        }

        for (int i = index - 1; i >= 0; i--) {
            queue.pushHistory(list.get(i));
        }

        play(selected);
    }

    /**
     * Advances playback based on the current RepeatMode.
     *
     * <p>Behavior depends on mode:
     * <ul>
     *   <li>LOOP_CURRENT_ONE → repeats the current track</li>
     *   <li>PLAY_ONE → stops playback after current track</li>
     *   <li>STOP_WHEN_QUEUE_END → plays next track or stops if queue is empty</li>
     *   <li>LOOP_CURRENT_QUEUE → loops entire queue when it reaches the end</li>
     * </ul>
     *
     * <p>This method is automatically triggered by the AudioEngine when a track finishes.</p>
     */
    public void playNext() {
        System.out.println("playing next");
        switch (repeatMode) {
            case LOOP_CURRENT_ONE -> {
                if (currentTrack != null) {
                    System.out.println("looping " + currentTrack);
                    play(currentTrack);
                }
            }

            case PLAY_ONE ->{
                System.out.println("Stoping");
                stop();
            }

            case STOP_WHEN_QUEUE_END -> {
                Track nextTrack = queue.next();
                if (nextTrack != null)
                    System.out.println("playing " + nextTrack);
                if (nextTrack != null) {
                    play(nextTrack);
                } else {
                    stop();
                }
            }

            case LOOP_CURRENT_QUEUE -> {
                Track nextTrack = queue.next();
                if (nextTrack != null)
                    System.out.println("playing " + nextTrack);
                if (nextTrack == null) {
                    queue.reset();
                    nextTrack = queue.next();
                }

                if (nextTrack != null) {
                    play(nextTrack);
                } else {
                    stop();
                }
            }
        }
    }

    /**
     * Moves to the previous track or restarts the current track.
     *
     * <p>If playback is at the beginning (progress == 0), it loads the previous track
     * from history. Otherwise, it resets the current track to the beginning.</p>
     */
    public void playPrev(){
        if (engine.getProgress()==0f) {
            if (currentTrack != null) {
                Track prevTrack = queue.prev();
                if (prevTrack != null) {
                    System.out.println("playing: " + prevTrack);
                    play(prevTrack);
                }
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

    /**
     * Sets playback volume.
     *
     * @param volume volume level (values below 0 are clamped to 0)
     */
    public void setVolume(int volume) {
        if (volume < 0) volume=0;
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

    @Override
    public String toString() {
        return "\nAudioPlayer{" +
                "\ncurrentTrack= " + currentTrack +
                ", \nqueue= " + queue +
                ", \nengine= " + engine +
                ", \nstate= " + state +
                ", \nrepeatMode= " + repeatMode +
                '}';
    }

    private String formatTime(long ms) {
        long seconds = ms / 1000;
        long mins = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }
}
