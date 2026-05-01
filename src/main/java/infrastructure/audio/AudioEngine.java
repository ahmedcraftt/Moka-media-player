package infrastructure.audio;

import java.nio.file.Path;

public interface AudioEngine {

    void play(Path path, Runnable onTrackFinished);
    void pause();
    void stop();
    void resume();
    boolean isPlaying();
    void setVolume(int volume);
    int getVolume();
    void setProgress(float position);
    float getProgress();
    void seek(float position);
    void release();
    void skipForwards(int seconds);
    void skipBackwards(int seconds);
    void setRepeat(boolean repeat);
    long getCurrentTime();
    long getTotalTime();
}
