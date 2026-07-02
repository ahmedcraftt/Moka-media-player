package infrastructure.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class ArtworkStorage {

    private final Path storageDir = Paths.get(System.getProperty("user.home"), ".moka_music_player", "artworks");

    public ArtworkStorage() {
        try {
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize artwork storage directory", e);
        }
    }

    public String saveArtwork(byte[] rawBytes, String mimeType) throws IOException {
        if (rawBytes == null || rawBytes.length == 0) {
            return null;
        }

        String extension = determineExtension(mimeType);

        String uniqueFilename = UUID.randomUUID() + extension;
        Path targetFile = storageDir.resolve(uniqueFilename);

        Files.write(targetFile, rawBytes);

        return targetFile.toAbsolutePath().toString();
    }

    private String determineExtension(String mimeType) {
        if (mimeType == null) return ".jpg";
        if (mimeType.contains("png")) return ".png";
        if (mimeType.contains("bmp")) return ".bmp";
        return ".jpg";
    }
}
