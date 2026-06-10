package infrastructure.media;

import domain.model.Track;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class DataResolver {

    public int resolveMissingDuration(Track track) {
        if (track.getMetadata().getDurationInSeconds() <= 0) {
            Path path = track.getFiledata().getFilePath();

            try {

                Process process = new ProcessBuilder(
                        "ffprobe",
                        "-v", "error",
                        "-show_entries", "format=duration",
                        "-of", "default=noprint_wrappers=1:nokey=1",
                        path.toString()
                ).start();

                try (BufferedReader reader =
                             new BufferedReader(
                                     new InputStreamReader(
                                             process.getInputStream()))) {

                    String line = reader.readLine();

                    process.waitFor();

                    if (line != null && !line.isBlank()) {

                        double seconds = Double.parseDouble(line);

                        return (int) Math.round(seconds);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0;
        }
        return track.getMetadata().getDurationInSeconds();
    }

    public String resolveMissingTitle(Track track) {
        if (track.getTitle() != null
                && !track.getTitle().isBlank()
                && !track.getTitle().equalsIgnoreCase("unknown")) {
            return track.getTitle();
        } else if (track.getFiledata().getFileName() != null
                && !track.getFiledata().getFileName().isBlank()
                && !track.getFiledata().getFileName().equalsIgnoreCase("unknown"))
            return removeExtension(track.getFiledata().getFileName());
        else return "unknown";
    }

    private String removeExtension(String fileName) {
        if (fileName == null) return null;

        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) return fileName;

        return fileName.substring(0, lastDot);
    }
}
