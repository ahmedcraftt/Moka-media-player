package application.dto;

public record MetadataDTO(
        int id,
        int durationInSeconds,
        long bitrate,
        long samplerate,
        String title,
        String genre,
        String description,
        String lyrics,
        String language,
        int year,
        String artworkPath,
        String series,
        String artist,
        String seriesArtist,
        int trackNumber
) {
}
