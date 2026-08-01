package infrastructure.scanner;

import domain.model.media.Track;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public interface MediaScanner {
    Track scan(File file);

    List<Track> scan(List<Path> paths);

    List<Track> scan(Path root);

    void scanForArtwork(Track track);

    boolean isAudioFile(Path path);
}
