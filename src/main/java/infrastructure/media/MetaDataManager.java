package infrastructure.media;

import domain.model.Track;

import java.nio.file.Path;

public interface MetaDataManager {
    void writeMetaData(Track track);
    void readMetadata(Track track);
}
