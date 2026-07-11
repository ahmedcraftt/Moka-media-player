package infrastructure.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VLCJAudioEngine implements AudioEngine {

    private static final Logger logger = LoggerFactory.getLogger(VLCJAudioEngine.class);

    private final MediaPlayer mediaPlayer;
    private final MediaPlayerFactory factory;
    private volatile Runnable currentOnFinished;

    private final ExecutorService callbackExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("audio-finished-callback-thread");
        thread.setDaemon(true);
        return thread;
    });

    public VLCJAudioEngine() {
        factory = new MediaPlayerFactory();
        mediaPlayer = factory.mediaPlayers().newMediaPlayer();

        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void finished(MediaPlayer mediaPlayer) {
                logger.debug("VLCJ native media player event hook: [FINISHED] triggered.");

                Runnable callback = currentOnFinished;

                if (callback != null) {
                    callbackExecutor.submit(callback);
                }
            }
        });

    }


    /**
     * Starts playback of the given media source.
     *
     * @param resource audio source
     * @param onTrackFinished callback triggered when playback completes
     */
    @Override
    public void play(URI resource, Runnable onTrackFinished) {
        this.currentOnFinished = onTrackFinished;

        logger.debug("Preparing native audio pipeline channel selection for resource: {}", resource);

        mediaPlayer.controls().stop();
        mediaPlayer.media().prepare(resource.toString());
        mediaPlayer.controls().play();
    }

    /**
     * Pauses current playback.
     */
    @Override
    public void pause() {
        mediaPlayer.controls().pause();
    }

    /**
     * Stops playback and resets media state.
     */
    @Override
    public void stop() {
        mediaPlayer.controls().stop();
    }

    /**
     * Resumes playback if paused.
     */
    @Override
    public void resume() {
        mediaPlayer.controls().play();
    }

    @Override
    public boolean isPlaying(){
        return mediaPlayer.status().isPlaying();
    }

    @Override
    public void setVolume(int volume) {
        mediaPlayer.audio().setVolume(volume);
    }

    @Override
    public int getVolume(){
        return mediaPlayer.audio().volume();
    }

    @Override
    public double getProgress() {
        return mediaPlayer.status().position(); // 0.0 to 1.0
    }

    @Override
    public void setProgress(double position){
        mediaPlayer.controls().setPosition((float) position);
    }

    @Override
    public long getCurrentTime() {
        return mediaPlayer.status().time();
    }

    @Override
    public long getTotalTime() {
        return mediaPlayer.status().length();
    }

    /**
     * Releases VLCJ resources.
     *
     * <p>Must be called when shutting down the application to avoid native memory leaks.</p>
     */
    @Override
    public void release() {
        logger.info("Initiating teardown sequences for native VLCJ audio layers...");

        mediaPlayer.release();
        factory.release();
        callbackExecutor.shutdown();
    }

    @Override
    public void skipForwards(int seconds) {
            long current = mediaPlayer.status().time();
            long length = mediaPlayer.status().length();

            long newTime = current + (seconds * 1000L);

            if (newTime > length) newTime = length;

            mediaPlayer.controls().setTime(newTime);
    }

    @Override
    public void skipBackwards(int seconds) {
        long current = mediaPlayer.status().time();

        long newTime = current - (seconds * 1000L);

        if (newTime < 0) newTime = 0;

        mediaPlayer.controls().setTime(newTime);
    }

    @Override
    public int getDuration() {
        return Math.toIntExact((mediaPlayer.media().info().duration()) / 1000);
    }

}
