package gui.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class TimeFormatter {

    private TimeFormatter() {
    }

    public static String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long mins = (seconds % 3600) / 60;
        long secs = seconds % 60;
        if (hours > 0) return String.format("[%02d:%02d:%02d]", hours, mins, secs);
        return String.format("[%02d:%02d]", mins, secs);
    }

    public static String formatDate(LocalDate date) {
        if (date == null) return "";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }

    public static String formatTime(LocalTime localTime) {
        if (localTime == null) return "";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return localTime.format(formatter);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy/MM/dd/HH:mm");

        return dateTime.format(formatter);
    }
}
