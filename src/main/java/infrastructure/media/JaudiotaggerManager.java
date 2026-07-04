package infrastructure.media;

import domain.model.metadata.Metadata;
import domain.model.media.Track;
import infrastructure.scanner.MediaScanException;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.UUID;

public class JaudiotaggerManager implements MetadataManager {

    private static final Logger logger = LoggerFactory.getLogger(JaudiotaggerManager.class);

    public void write(Track track) {
        logger.debug("Writing metadata for track: {}", track.getFilePath());
        File file = null;

        try {
            file = new File(track.getFiledata().getFilePath().toUri());

            if (!file.exists() || file.length() < 1024) {
                throw new IllegalArgumentException("Invalid or corrupted audio file: " + file);
            }

            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTagOrCreateAndSetDefault();
            Metadata metadata = track.getMetadata();

            safeSet(tag, FieldKey.TITLE, track.getTitle());
            safeSet(tag, FieldKey.GENRE, metadata.getGenre());
            safeSet(tag, FieldKey.YEAR, String.valueOf(metadata.getYear()));
            safeSet(tag, FieldKey.COMMENT, metadata.getDescription());
            safeSet(tag, FieldKey.LANGUAGE, metadata.getLanguage().toString());
            safeSet(tag, FieldKey.LYRICS, metadata.getLyrics());

            safeSet(tag, FieldKey.ARTIST, metadata.getArtist());
            safeSet(tag, FieldKey.ALBUM, metadata.getSeries());
            safeSet(tag, FieldKey.ALBUM_ARTIST, metadata.getSeriesArtist());
            safeSet(tag, FieldKey.TRACK, String.valueOf(metadata.getTrackNumber()));

            writeArtwork(metadata, tag);

            audioFile.commit();

        } catch (Exception e) {
            logger.error("Failed to write metadata for file: {}", file, e);
        }
    }

    public void read(Track track) {
        try {
            Metadata metadata = track.getMetadata();
            logger.debug("Reading metadata for: {}", track.getFiledata().getFilePath());

            File file = new File(track.getResource());
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();
            AudioHeader header = audioFile.getAudioHeader();

            if (header == null) {
                throw new IllegalArgumentException("Invalid or corrupted audio file: " + file);
            }

            if (tag != null) {
                metadata.setTitle(tag.getFirst(FieldKey.TITLE));
                metadata.setGenre(tag.getFirst(FieldKey.GENRE));
                metadata.setYear(safeParseYear(tag.getFirst(FieldKey.YEAR)));
                metadata.setLanguage(tag.getFirst(FieldKey.LANGUAGE));
                metadata.setDurationInSeconds(header.getTrackLength());

                Integer br = null;
                try {
                    br = Math.toIntExact(header.getBitRateAsNumber());
                } catch (Exception ignored) {}

                if (br != null) {
                    metadata.setBitrate(br);
                }

                // Cleaned up the duplicate samplerate mutation statement here
                metadata.setSamplerate(header.getSampleRateAsNumber());
                metadata.setDescription(tag.getFirst(FieldKey.COMMENT));
                metadata.setLyrics(tag.getFirst(FieldKey.LYRICS));

                metadata.setArtist(tag.getFirst(FieldKey.ARTIST));
                metadata.setSeries(tag.getFirst(FieldKey.ALBUM));
                metadata.setSeriesArtist(tag.getFirst(FieldKey.ALBUM_ARTIST));
                metadata.setTrackNumber(safeParseInt(tag.getFirst(FieldKey.TRACK)));
            }

        } catch (Exception e) {
            logger.error("Metadata read failed for path: {}", track.getFiledata().getFilePath(), e);
        }
    }

    @Override
    public byte[] extractRawArtworkBytes(Path path) {
        if (path == null) {
            return null;
        }

        try {
            File file = path.toFile();
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();

            if (tag != null) {
                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    return artwork.getBinaryData();
                }
            }
        } catch (Exception e) {
            logger.error("Failed to extract artwork from path: {}", path, e);
        }

        return null;
    }

    private void writeArtwork(Metadata metadata, Tag tag) throws
            FieldDataInvalidException, IOException {
        String artworkPath = metadata.getArtworkPath();

        if (artworkPath != null && !artworkPath.isBlank()) {
            File artworkFile = new File(artworkPath);

            if (artworkFile.exists()) {
                tag.deleteArtworkField();
                Artwork artwork = ArtworkFactory.createArtworkFromFile(artworkFile);
                tag.setField(artwork);
            }
        } else {
            tag.deleteArtworkField();
        }
    }

    private int safeParseInt(String value) {
        try {
            return (value == null || value.isBlank()) ? 0 : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static Year safeParseYear(String yearStr) {
        if (yearStr == null || yearStr.isBlank()) return null;

        try {
            return Year.parse(yearStr);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void safeSet(Tag tag, FieldKey key, String value) throws Exception {
        if (value != null) {
            value = value.trim();
            if (!value.isEmpty()) {
                tag.setField(key, value);
            }
        }
    }
}