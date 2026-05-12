package infrastructure.media;

import domain.model.Filedata;
import domain.model.MediaType;
import domain.model.Track;
import domain.model.Metadata;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.nio.file.Path;
import java.time.Year;
import java.time.format.DateTimeParseException;

public class JaudiotaggerManager implements MetadataManager {

    public void write(Track track) {
        try {
            File file = new File(track.getFileData().getFilePath().toUri());
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTagOrCreateAndSetDefault();
            Metadata metadata = track.getMetadata();

            if (tag == null) {
                tag = audioFile.createDefaultTag();
                audioFile.setTag(tag);
            }

            safeSet(tag, FieldKey.TITLE, track.getTitle());
            safeSet(tag,FieldKey.GENRE,metadata.getGenre());
            safeSet(tag,FieldKey.YEAR, String.valueOf(metadata.getYear()));
            safeSet(tag,FieldKey.COMMENT,metadata.getDescription());
            if (track.getType()== MediaType.SONG){
                safeSet(tag,FieldKey.ARTIST,metadata.getArtist());
                safeSet(tag,FieldKey.ALBUM,metadata.getAlbum());
                safeSet(tag, FieldKey.ALBUM_ARTIST, metadata.getAlbumArtist());
                safeSet(tag,FieldKey.LYRICS,metadata.getLyrics());
            }
            if (track.getType()== MediaType.PODCAST){
                safeSet(tag, FieldKey.ARTIST, metadata.getHost());
                safeSet(tag,FieldKey.ALBUM,metadata.getChannel());
                safeSet(tag,FieldKey.TRACK, String.valueOf(metadata.getEpisodeNumber()));
            }
            if (track.getType()==MediaType.AUDIOBOOK){
                safeSet(tag,FieldKey.ARTIST,metadata.getNarrator());
                safeSet(tag,FieldKey.ALBUM,metadata.getSeries());
                safeSet(tag,FieldKey.ALBUM_ARTIST,metadata.getAuthor());
                safeSet(tag, FieldKey.TRACK, String.valueOf(metadata.getChapterCount()));
            }
            audioFile.commit();
        } catch (Exception e){
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void read(Track track) {

        try {
            Metadata metadata = track.getMetadata();
            Filedata fileData = track.getFileData();

            File file = new File(fileData.getFilePath().toUri());
            Path path = Path.of(fileData.getFilePath().toUri());

            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();
            AudioHeader header = audioFile.getAudioHeader();

            if(tag != null) {

                String title = tag.getFirst(FieldKey.TITLE);
                if(title == null || title.isBlank() || title.equalsIgnoreCase("unknown")) {
                    title = file.getName();
                }
                track.setTitle(title.trim());
                metadata.setGenre(tag.getFirst(FieldKey.GENRE));
                metadata.setYear(safeParseYear(tag.getFirst(FieldKey.YEAR)));
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
                if (track.getType()== MediaType.SONG){
                    metadata.setArtist(tag.getFirst(FieldKey.ARTIST));
                    metadata.setAlbum(tag.getFirst(FieldKey.ALBUM));
                    metadata.setAlbumArtist(tag.getFirst(FieldKey.ALBUM_ARTIST));
                    metadata.setLyrics(tag.getFirst(FieldKey.LYRICS));
                }
                if (track.getType()== MediaType.PODCAST){
                    metadata.setChannel(tag.getFirst(FieldKey.ALBUM));
                    metadata.setHost(tag.getFirst(FieldKey.ARTIST));
                    metadata.setEpisodeNumber(safeParseInt(tag.getFirst(FieldKey.TRACK)));
                }
                if (track.getType()==MediaType.AUDIOBOOK){
                    metadata.setAuthor(tag.getFirst(FieldKey.ALBUM_ARTIST));
                    metadata.setNarrator(tag.getFirst(FieldKey.ARTIST));
                    metadata.setSeries(tag.getFirst(FieldKey.ALBUM));
                    metadata.setChapterCount(safeParseInt(tag.getFirst(FieldKey.TRACK)));
                }
            }

        } catch(Exception e) {
            System.err.println("Metadata read failed for: " + track.getFileData().getFilePath());
            e.printStackTrace();
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
        if (value != null && !value.isBlank()) {
            tag.setField(key, value);
        }
    }

}
