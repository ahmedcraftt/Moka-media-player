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
                metadata.getTitle() != null ? metadata.getTitle() : "Unknown Title",
                metadata.getGenre() != null ? metadata.getGenre() : "Unknown Genre",
                metadata.getDescription() != null ? metadata.getDescription() : "",
                metadata.getLyrics() != null ? metadata.getLyrics() : "",
                languageStr,
                metadata.getYear().getValue(),
                metadata.getArtworkPath() != null ? metadata.getArtworkPath() : "",
                metadata.getSeries() != null ? metadata.getSeries() : "",
                metadata.getArtist() != null ? metadata.getArtist() : "Unknown Artist",
                metadata.getSeriesArtist() != null ? metadata.getSeriesArtist() : "",
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
