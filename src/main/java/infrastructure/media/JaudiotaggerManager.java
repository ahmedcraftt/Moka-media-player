package infrastructure.media;

import entities.MediaType;
import entities.Track;

import entities.TrackMetadata;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public class JaudiotaggerManager implements MetaDataManager {

    public void writeMetaData(Track track){
        try {
            File file = new File(track.getFilePath().toUri());
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTagOrCreateAndSetDefault();
            TrackMetadata metadata = track.getMetadata();

            safeSet(tag,FieldKey.TITLE, metadata.getTitle());
            safeSet(tag,FieldKey.GENRE,metadata.getGenre());
            safeSet(tag,FieldKey.YEAR, String.valueOf(metadata.getYear()));
            safeSet(tag,FieldKey.COMMENT,metadata.getDescription());
            if (track.getType()== MediaType.SONG){
                safeSet(tag,FieldKey.ARTIST,metadata.getArtist());
                safeSet(tag,FieldKey.ALBUM,metadata.getAlbum());
                safeSet(tag,FieldKey.LYRICS,metadata.getLyrics());
            }
            if (track.getType()== MediaType.PODCAST){
                safeSet(tag,FieldKey.ARTIST,metadata.getArtist());
                safeSet(tag,FieldKey.ALBUM,metadata.getChannel());
                safeSet(tag,FieldKey.TRACK, String.valueOf(metadata.getEpisodeNumber()));
            }
            if (track.getType()==MediaType.AUDIOBOOK){
                safeSet(tag,FieldKey.ARTIST,metadata.getNarrator());
                safeSet(tag,FieldKey.ALBUM,metadata.getSeries());
                safeSet(tag,FieldKey.ALBUM_ARTIST,metadata.getAuthor());
            }
            audioFile.commit();
        } catch (Exception e){
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void readMetadata(Track track) {

        try {
            TrackMetadata metadata = track.getMetadata();

            File file = new File(track.getFilePath().toUri());
            Path path = Path.of(track.getFilePath().toUri());
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);

            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();
            AudioHeader header = audioFile.getAudioHeader();

            if(tag != null) {

                String title = tag.getFirst(FieldKey.TITLE);
                if(title == null || title.isBlank() || title.equalsIgnoreCase("unknown")) {
                    title = file.getName();
                }
                metadata.setTitle(title.trim());
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

                org.jaudiotagger.tag.images.Artwork artwork = tag.getFirstArtwork();
                if (artwork != null && artwork.getBinaryData() != null) {
                    metadata.setArtwork(artwork.getBinaryData());
                }
                track.setFileSize(attributes.size());
                metadata.setSampleRate(header.getSampleRateAsNumber());
                track.setDateCreated(toLocalDate(attributes.creationTime()));
                track.setDateModified(toLocalDate(attributes.lastModifiedTime()));
                metadata.setDescription(tag.getFirst(FieldKey.COMMENT));
                if (track.getType()== MediaType.SONG){
                    metadata.setArtist(tag.getFirst(FieldKey.ARTIST));
                    metadata.setAlbum(tag.getFirst(FieldKey.ALBUM));
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
            System.err.println("Metadata read failed for: " + track.getFilePath());
            e.printStackTrace();
        }

    }

    public int getDuration(Path path) {
        AudioFile audioFile = createAudioFile(path);
        AudioHeader header = audioFile.getAudioHeader();
        return header.getTrackLength();
    }

    public String getGenre(Path path){
       AudioFile audioFile = createAudioFile(path);
       Tag tag = audioFile.getTagOrCreateAndSetDefault();
       return tag.getFirst(FieldKey.GENRE);
    }

    public String getTitle(Path path){
        AudioFile audioFile = createAudioFile(path);
        Tag tag = audioFile.getTagOrCreateAndSetDefault();
        return tag.getFirst(FieldKey.TITLE);
    }

    public String getTrackNumber (Path path){
        AudioFile audioFile = createAudioFile(path);
        Tag tag = audioFile.getTagOrCreateAndSetDefault();
        return tag.getFirst(FieldKey.TRACK);
    }

    private AudioFile createAudioFile(Path path){
        File file = new File(path.toUri());
        AudioFile audioFile;
        try {
            audioFile = AudioFileIO.read(file);
            return audioFile;
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            throw new RuntimeException(e);
        }
    }

    private LocalDate toLocalDate(FileTime fileTime){
        return fileTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
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
