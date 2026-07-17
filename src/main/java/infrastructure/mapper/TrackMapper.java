package infrastructure.mapper;

import application.dto.TrackDTO;
import domain.model.media.MediaType;
import domain.model.media.Track;
import domain.model.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TrackMapper {

    private static final Logger logger = LoggerFactory.getLogger(TrackMapper.class);

    public static TrackDTO toDTO(Track track) {
        long modified;
        try {
            modified = Files.getLastModifiedTime(track.getFiledata().getFilePath()).toMillis();
        } catch (IOException e) {
            throw new RuntimeException("Cannot read or parse last modified time of " + track.getFiledata().getFilePath(), e);
        }

        logger.debug("Mapping metadata ID: {}", track.getMetadata().getId());

        String lastPlayed;
        if (track.getLastPlayed() != null) {
            lastPlayed = track.getLastPlayed().toString();
        } else lastPlayed = "none";


        return new TrackDTO(
                track.getMetadata().getId()
                , track.isFavorite()
                , track.getTimesPlayed()
                , String.valueOf(track.getFiledata().getFilePath())
                , track.getType().getTitle()
                , modified
                , track.getFiledata().getFileSize()
                , track.getDateAdded().toString()
                , track.getFiledata().getDateCreatedString()
                , track.getFiledata().getLastAccessedString()
                , track.getFiledata().getFileType()
                , lastPlayed
        );
    }

    public static Track fromDTO(TrackDTO dto, Metadata metadata) {
        Path path = Path.of(dto.path());
        String fileName = path.getFileName().toString();
        LocalDate dateModified = Instant.ofEpochMilli(dto.lastModified()).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDateTime lastPlayed;
        String strLastPlayed = dto.lastPlayed();

        if (strLastPlayed == null || strLastPlayed.isBlank() || strLastPlayed.equals("none")) {
            lastPlayed = null;
        } else lastPlayed = LocalDateTime.parse(dto.lastPlayed());

        return new Track(
                metadata,
                dto.metadataId(),
                dto.favorite(),
                dto.timesPlayed(),
                MediaType.StringToMediaType(dto.type()),
                LocalDate.parse(dto.dateAdded()),
                path,
                fileName,
                LocalDate.parse(dto.dateCreated()),
                dateModified,
                LocalDate.parse(dto.lastAccessed()),
                dto.fileType(),
                dto.size(),
                lastPlayed
        );
    }
}