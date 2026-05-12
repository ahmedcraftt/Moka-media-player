package infrastructure.storge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import domain.model.Playlist;
import application.dto.PlaylistDTO;
import domain.model.Track;
import infrastructure.media.JaudiotaggerManager;
import infrastructure.media.MetadataManager;
import infrastructure.factory.TrackFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class PlaylistStorage {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Path base = Path.of(System.getProperty("user.home"), ".moka_music_player", "playlists.json");

    public static void save(Playlist playlist) throws IOException {

        PlaylistDTO dto = new PlaylistDTO();
        dto.setTitle(playlist.getTitle());
        dto.setFavorite(playlist.isFavorite());

        dto.setTrackPaths(playlist.getTracks()
                .stream()
                .map(t -> t.getFilePath().toString())
                .collect(Collectors.toList()));

        Files.createDirectories(base.getParent());
        Files.writeString(base, gson.toJson(dto));
    }
    public static Playlist load() throws IOException {

        String json = Files.readString(base);
        PlaylistDTO dto = gson.fromJson(json, PlaylistDTO.class);

        Playlist playlist = new Playlist(dto.getTitle(), dto.isFavorite());

        for (String pathStr : dto.trackPaths) {
            Path path = Path.of(pathStr);
            String filename = path.getFileName().toString();
            Track t = TrackFactory.create(path);

            playlist.addTrack(t);
        }

        return playlist;
    }
}