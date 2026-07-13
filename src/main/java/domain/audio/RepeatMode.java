package domain.audio;

public enum RepeatMode {
    PLAY_ONE("Play 1"),
    LOOP_CURRENT_ONE("Loop 1"),
    STOP_WHEN_QUEUE_END("Play Queue"),
    LOOP_CURRENT_QUEUE("Loop queue");
    private final String text;

    RepeatMode(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static RepeatMode fromString(String text) {
        if (text == null) return PLAY_ONE;
        for (RepeatMode mode : values()) {
            if (mode.text.equals(text)) return mode;
        }
        return PLAY_ONE;
    }

}
