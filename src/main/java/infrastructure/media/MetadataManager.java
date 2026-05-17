package infrastructure.media;

import domain.model.Track;

public interface MetadataManager {
    void write(Track track);
    void read(Track track);
}
