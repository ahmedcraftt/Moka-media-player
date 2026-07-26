package config;

import domain.audio.RepeatMode;

public final class PlayerConfig {

    private static final int DEFAULT_VOLUME_LEVEL = 50;
    private static final int DEFAULT_SKIP_SECONDS = 10;
    private static final int DEFAULT_VOLUME_MODIFIER = 10;
    private static final RepeatMode DEFAULT_REPEAT_MODE = RepeatMode.STOP_WHEN_QUEUE_END;

    private int preferredVolumeLevel = DEFAULT_VOLUME_LEVEL;

    private int preferredVolumeModifier = DEFAULT_VOLUME_MODIFIER;

    private int preferredSkipSeconds = DEFAULT_SKIP_SECONDS;

    private RepeatMode preferredRepeatMode = DEFAULT_REPEAT_MODE;

    private boolean shuffle = false;

    PlayerConfig() {
    }

    public int getPreferredSkipSeconds() {
        return preferredSkipSeconds;
    }

    public void setPreferredSkipSeconds(int preferredSkipSeconds) {
        this.preferredSkipSeconds = preferredSkipSeconds;
    }

    public int getPreferredVolumeLevel() {
        return preferredVolumeLevel;
    }

    public void setPreferredVolumeLevel(int preferredVolumeLevel) {
        this.preferredVolumeLevel = preferredVolumeLevel;
    }

    public int getPreferredVolumeModifier() {
        return preferredVolumeModifier;
    }

    public void setPreferredVolumeModifier(int preferredVolumeModifier) {
        this.preferredVolumeModifier = preferredVolumeModifier;
    }

    public RepeatMode getPreferredRepeatMode() {
        return preferredRepeatMode;
    }

    public void setPreferredRepeatMode(RepeatMode preferredRepeatMode) {
        this.preferredRepeatMode = preferredRepeatMode;
    }

    public boolean isShuffle() {
        return shuffle;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }
}
