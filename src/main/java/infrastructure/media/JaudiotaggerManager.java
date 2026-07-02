package infrastructure.media;

import domain.model.metadata.Metadata;
import domain.model.media.Track;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;

import java.io.File;
import java.nio.file.Path;
import java.time.Year;
import java.time.format.DateTimeParseException;

public class JaudiotaggerManager implements MetadataManager {

    public void write(Track track) {
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

            audioFile.commit();

        } catch (Exception e) {
            System.err.println("Failed to write metadata for: " + file);
        }
    }

    public void read(Track track) {

        try {

            Metadata metadata = track.getMetadata();

            File file = new File(track.getResource());

            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();
            AudioHeader header = audioFile.getAudioHeader();

            if (header == null) {
                throw new IllegalArgumentException("Invalid or corrupted audio file: " + file);
            }

            if(tag != null) {

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

                int sr = header.getSampleRateAsNumber();
                metadata.setSamplerate(sr);

                metadata.setSamplerate(header.getSampleRateAsNumber());
                metadata.setDescription(tag.getFirst(FieldKey.COMMENT));
                metadata.setLyrics(tag.getFirst(FieldKey.LYRICS));

                metadata.setArtist(tag.getFirst(FieldKey.ARTIST));
                metadata.setSeries(tag.getFirst(FieldKey.ALBUM));
                metadata.setSeriesArtist(tag.getFirst(FieldKey.ALBUM_ARTIST));
                metadata.setTrackNumber(safeParseInt(tag.getFirst(FieldKey.TRACK)));

            }

        } catch(Exception e) {
            System.err.println("Metadata read failed for: " + track.getFiledata().getFilePath());
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
            System.err.println("Failed to extract artwork from: " + path + " - " + e.getMessage());
        }

        return null;
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
