package infrastructure.media;

import domain.model.media.Track;

public interface MetadataManager {
    void write(Track track);
    void read(Track track);
}
