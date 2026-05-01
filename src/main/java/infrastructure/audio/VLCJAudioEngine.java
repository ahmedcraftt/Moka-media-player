package infrastructure.audio;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

import java.nio.file.Path;

public class VLCJAudioEngine implements AudioEngine {
    private final MediaPlayer mediaPlayer;
    private final MediaPlayerFactory factory;
    private Runnable currentOnFinished;

    public VLCJAudioEngine() {
        factory = new MediaPlayerFactory();
        mediaPlayer = factory.mediaPlayers().newMediaPlayer();

        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void finished(MediaPlayer mediaPlayer) {
                System.out.println("FINISHED EVENT FIRED");
                if (currentOnFinished != null) {
                    new Thread(() -> {
                        try {
                            Thread.sleep(50);
                            currentOnFinished.run();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }
        });

    }

    @Override
    public void play(Path path, Runnable onTrackFinished) {
        this.currentOnFinished = onTrackFinished;
        mediaPlayer.media().play(path.toAbsolutePath().toString());
    }

    @Override
    public void pause() {
        mediaPlayer.controls().pause();
    }

    @Override
    public void stop() {
        mediaPlayer.controls().stop();
    }

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

    public int getVolume(){
        return mediaPlayer.audio().volume();
    }

    @Override
    public float getProgress() {
        return mediaPlayer.status().position(); // 0.0 to 1.0
    }

    public void setProgress(float position){
        mediaPlayer.controls().setPosition(position);
    }

    @Override
    public void seek(float position) {
        mediaPlayer.controls().setPosition(position); // 0.0 to 1.0
    }

    @Override
    public void setRepeat(boolean repeat){
        mediaPlayer.media().setRepeat(repeat);
    }

    @Override
    public long getCurrentTime() {
        return mediaPlayer.status().time();
    }

    @Override
    public long getTotalTime() {
        return mediaPlayer.status().length();
    }

    @Override
    public void release() {
        mediaPlayer.release();
        factory.release();
    }

    @Override
    public void skipForwards(int seconds) {
            long current = mediaPlayer.status().time();
            long length = mediaPlayer.status().length();

            long newTime = current + (seconds * 1000L);

            // Clamp to track length
            if (newTime > length) newTime = length;

            mediaPlayer.controls().setTime(newTime);
    }

    @Override
    public void skipBackwards(int seconds) {
        long current = mediaPlayer.status().time();

        long newTime = current - (seconds * 1000L);

        // Clamp to 0
        if (newTime < 0) newTime = 0;

        mediaPlayer.controls().setTime(newTime);
    }


}
