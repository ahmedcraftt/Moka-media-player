package infrastructure.scanner;

import domain.model.media.Track;

import java.nio.file.Path;
import java.util.List;

public interface ScanListener {
    void onTrackFound(Track track);

    void onScanFinished(List<Track> tracks);

    void onError(Path path, Exception e);
}