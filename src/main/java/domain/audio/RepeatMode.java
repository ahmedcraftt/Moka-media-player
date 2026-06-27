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

}
