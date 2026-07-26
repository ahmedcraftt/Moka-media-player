package infrastructure.mapper;

import application.dto.MetadataDTO;
import domain.model.metadata.Metadata;

import java.time.Year;

public class MetadataMapper {
    public static MetadataDTO toDTO(Metadata metadata) {
        String languageStr = (metadata.getLanguage() != null)
                ? metadata.getLanguage().getLanguageString()
                : "Unknown";

        return new MetadataDTO(
                metadata.getId(),
                metadata.getDurationInSeconds(),
                metadata.getBitrate(),
                metadata.getSamplerate(),
                metadata.getTitle(),
                metadata.getGenre(),
                metadata.getDescription(),
                metadata.getLyrics(),
                languageStr,
                metadata.getYear().getValue(),
                metadata.getArtworkPath(),
                metadata.getSeries(),
                metadata.getArtist(),
                metadata.getSeriesArtist(),
                metadata.getTrackNumber()
        );
    }

    public static Metadata fromDTO(MetadataDTO dto) {
        return new Metadata(
                dto.id(),
                dto.durationInSeconds(),
                dto.bitrate(),
                dto.samplerate(),
                dto.title(),
                dto.genre(),
                dto.description(),
                dto.lyrics(),
                dto.language(),
                Year.of(dto.year()),
                dto.artworkPath(),
                dto.series(),
                dto.artist(),
                dto.seriesArtist(),
                dto.trackNumber()
        );
    }
}
