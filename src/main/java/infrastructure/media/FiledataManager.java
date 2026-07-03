package infrastructure.media;

import domain.model.metadata.Filedata;
import domain.model.media.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.ZoneId;

public class FiledataManager {

    private static final Logger logger = LoggerFactory.getLogger(FiledataManager.class);

    public void read(Track track) {

        Filedata data = track.getFiledata();
        Path path = Path.of(data.getFilePath().toUri());

        try {
            logger.debug("Reading filedata of track: {}", path);

            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);

            data.setDateCreated(toLocalDate(attributes.creationTime()));
            data.setDateModified(toLocalDate(attributes.lastModifiedTime()));
            data.setLastAccessed(toLocalDate(attributes.lastAccessTime()));
            data.setFileSize(attributes.size());
            data.setFileType(Files.probeContentType(path));

            logger.debug("track data: {}", data);

        } catch (IOException e) {
            logger.error("File data read failed for: {}", track.getFiledata().getFilePath(), e);
        }
    }

    private LocalDate toLocalDate(FileTime fileTime) {
        return fileTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}