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

    int getPreferredSkipSeconds() {
        return preferredSkipSeconds;
    }

    void setPreferredSkipSeconds(int preferredSkipSeconds) {
        this.preferredSkipSeconds = preferredSkipSeconds;
    }

    int getPreferredVolumeLevel() {
        return preferredVolumeLevel;
    }

    void setPreferredVolumeLevel(int preferredVolumeLevel) {
        this.preferredVolumeLevel = preferredVolumeLevel;
    }

    int getPreferredVolumeModifier() {
        return preferredVolumeModifier;
    }

    void setPreferredVolumeModifier(int preferredVolumeModifier) {
        this.preferredVolumeModifier = preferredVolumeModifier;
    }

    RepeatMode getPreferredRepeatMode() {
        return preferredRepeatMode;
    }

    void setPreferredRepeatMode(RepeatMode preferredRepeatMode) {
        this.preferredRepeatMode = preferredRepeatMode;
    }

    boolean isShuffle() {
        return shuffle;
    }

    void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }
}
