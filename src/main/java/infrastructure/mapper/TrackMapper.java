package infrastructure.mapper;

import application.dto.TrackDTO;
import domain.model.MediaType;
import domain.model.Track;
import infrastructure.factory.TrackFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

public class TrackMapper {

    public static TrackDTO toDTO(Track track) {
        long modified;
        try {
            modified = Files.getLastModifiedTime(track.getFiledata().getFilePath()).toMillis();
        } catch (IOException e) {
            throw new RuntimeException("Cannot read or parse last modified time of " + track.getFiledata().getFilePath());
        }
        return new TrackDTO(
                track.getTitle()
                , track.isFavorite()
                , track.getTimesPlayed()
                , String.valueOf(track.getFiledata().getFilePath())
                , track.getType().getTitle()
                , modified
                , track.getFiledata().getFileSize()
                , track.getDateAdded().toString()
        );
    }

    public static Track fromDTO(TrackDTO dto) {
        return TrackFactory.create(
                dto.title(),
                dto.favorite(),
                dto.timesPlayed(),
                MediaType.StringToMediaType(dto.type()),
                Path.of(dto.path()),
                LocalDate.parse(dto.dateAdded())
        );

    }

}
