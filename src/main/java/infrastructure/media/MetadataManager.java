package infrastructure.media;

import domain.model.media.Track;

import java.nio.file.Path;

public interface MetadataManager {
    void write(Track track);
    void read(Track track);
    byte[] extractRawArtworkBytes(Path path);

}
