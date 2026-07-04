package infrastructure.audio;

import java.net.URI;

public interface AudioEngine {

    /**
     * Low-level audio playback abstraction.
     *
     * <p>AudioEngine implementations are responsible for interacting directly
     * with a media backend (e.g. VLCJ) and exposing playback operations
     * to higher-level systems such as AudioPlayer.</p>
     *
     * <h2>Responsibilities</h2>
     * <ul>
     *   <li>Media playback control</li>
     *   <li>Volume and seeking operations</li>
     *   <li>Playback state reporting</li>
     *   <li>Track completion callbacks</li>
     *   <li>Resource management and cleanup</li>
     * </ul>
     *
     * <h2>Important Notes</h2>
     * <ul>
     *   <li>This interface does not manage queue logic or playback order.</li>
     *   <li>Implementations may use asynchronous event systems internally.</li>
     *   <li>{@code onTrackFinished} callbacks should be executed safely and predictably.</li>
     * </ul>
     */

    void play(URI resource, Runnable onTrackFinished);
    void pause();
    void stop();
    void resume();
    boolean isPlaying();
    void setVolume(int volume);
    int getVolume();
    void setProgress(double position);
    double getProgress();
    void release();
    void skipForwards(int seconds);
    void skipBackwards(int seconds);
    long getCurrentTime();
    long getTotalTime();

}
