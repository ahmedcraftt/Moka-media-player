package infrastructure.media;

import domain.model.metadata.Filedata;
import domain.model.media.Track;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.ZoneId;

public class FiledataManager {

    public void read(Track track) {

        Filedata data = track.getFiledata();

        File file = new File(data.getFilePath().toUri());
        Path path = Path.of(data.getFilePath().toUri());

        try {
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);

            data.setDateCreated(toLocalDate(attributes.creationTime()));
            data.setDateModified(toLocalDate(attributes.lastModifiedTime()));
            data.setLastAccessed(toLocalDate(attributes.lastAccessTime()));
            data.setFileSize(attributes.size());
            data.setFileType(Files.probeContentType(path));

        } catch (IOException e) {
            System.err.println("file data read failed for: " + track.getFiledata().getFilePath());
            e.printStackTrace();
        }
    }

    private LocalDate toLocalDate(FileTime fileTime) {
        return fileTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
