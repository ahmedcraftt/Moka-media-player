package infrastructure.media;

import domain.model.metadata.MediaMetadata;
import domain.model.metadata.Metadata;
import domain.model.media.Track;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

import java.io.File;
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

            MediaMetadata mediaMetadata = track.getMediaMetadata();

            safeSet(tag, FieldKey.TITLE, track.getTitle());
            safeSet(tag, FieldKey.GENRE, metadata.getGenre());
            safeSet(tag, FieldKey.YEAR, String.valueOf(metadata.getYear()));
            safeSet(tag, FieldKey.COMMENT, metadata.getDescription());
            safeSet(tag, FieldKey.LANGUAGE, normalizeLanguage(metadata.getLanguage()));
            safeSet(tag, FieldKey.LYRICS, metadata.getLyrics());

            safeSet(tag, FieldKey.ARTIST, mediaMetadata.getArtist());
            safeSet(tag, FieldKey.ALBUM, mediaMetadata.getSeries());
            safeSet(tag, FieldKey.ALBUM_ARTIST, mediaMetadata.getSeriesArtist());
            safeSet(tag, FieldKey.TRACK, String.valueOf(mediaMetadata.getTrackNumber()));

            audioFile.commit();

        } catch (Exception e) {
            System.err.println("Failed to write metadata for: " + file);
            e.printStackTrace();
        }
    }

    public void read(Track track) {

        try {

            Metadata metadata = track.getMetadata();

            MediaMetadata mediaMetadata = track.getMediaMetadata();

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
                metadata.setSampleRate(sr);

                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null && artwork.getBinaryData() != null) {
                    metadata.setArtwork(artwork.getBinaryData());
                }
                metadata.setSampleRate(header.getSampleRateAsNumber());
                metadata.setDescription(tag.getFirst(FieldKey.COMMENT));
                metadata.setLyrics(tag.getFirst(FieldKey.LYRICS));

                mediaMetadata.setArtist(tag.getFirst(FieldKey.ARTIST));
                mediaMetadata.setSeries(tag.getFirst(FieldKey.ALBUM));
                mediaMetadata.setSeriesArtist(tag.getFirst(FieldKey.ALBUM_ARTIST));
                mediaMetadata.setTrackNumber(safeParseInt(tag.getFirst(FieldKey.TRACK)));

            }

        } catch(Exception e) {
            System.err.println("Metadata read failed for: " + track.getFiledata().getFilePath());
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

    private String normalizeLanguage(String lang) {
        if (lang == null) return null;

        lang = lang.trim().toLowerCase();

        return switch (lang) {
            case "english", "eng" -> "eng";
            case "arabic", "ara" -> "ara";
            case "japanese", "jpn" -> "jpn";
            case "french", "fra" -> "fra";
            case "german", "deu" -> "deu";
            case "spanish", "spa" -> "spa";
            default -> lang;
        };
    }

}
