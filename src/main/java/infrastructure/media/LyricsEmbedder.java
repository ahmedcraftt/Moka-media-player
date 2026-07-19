package infrastructure.media;

import domain.model.media.Track;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;

public class LyricsEmbedder {

    private static final Logger logger = LoggerFactory.getLogger(LyricsEmbedder.class);

    private final MetadataManager metadataManager;

    public LyricsEmbedder(MetadataManager metadataManager) {
        this.metadataManager = metadataManager;
    }

    public void embedLyrics(Track track, Path lyricsPath) {
        if (!Files.isRegularFile(lyricsPath)) return;
        String lyrics;
        try {
            lyrics = Files.readString(lyricsPath);
            track.getMetadata().setLyrics(lyrics);
            metadataManager.write(track);
        } catch (IOException e) {
            logger.error("Error reading Lyrics from path: {}", lyricsPath, e);
        }
    }

    public void embedLyrics(Track track) {
        String baseName = getFileNameWithoutExtension(track.getFilePath());

        Path lyricsFile = track.getFilePath().getParent().resolve(baseName + ".lrc");

        embedLyrics(track, lyricsFile);
    }

    public void embedLyrics(List<Track> tracks) {
        for (Track track : tracks) {
            embedLyrics(track);
        }
    }

    private String getFileNameWithoutExtension(Path path) {
        String fileName = path.getFileName().toString();
        int lastDot = fileName.lastIndexOf('.');
        return (lastDot == -1) ? fileName : fileName.substring(0, lastDot);
    }
}