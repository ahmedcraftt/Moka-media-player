package gui.utils;

public class TimeFormater {

    public static String formatTime(long seconds) {
        long hours = seconds / 3600;
        long mins = (seconds % 3600) / 60;
        long secs = seconds % 60;
        if (hours > 0) return String.format("[%02d:%02d:%02d]", hours, mins, secs);
        return String.format("[%02d:%02d]", mins, secs);
    }
}
