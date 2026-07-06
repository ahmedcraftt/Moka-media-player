package infrastructure.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import domain.model.media.Playlist;
import application.dto.PlaylistDTO;
import infrastructure.mapper.PlaylistMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class PlaylistStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path BASE_DIR =
            Path.of(
                    System.getProperty("user.home"),
                    ".moka_music_player",
                    "playlists"
            );

    public static void save(Playlist playlist) throws IOException {

        PlaylistDTO dto = PlaylistMapper.toDTO(playlist);

        Files.createDirectories(BASE_DIR);

        Path path =
                BASE_DIR.resolve(dto.title() + ".json");

        Files.writeString(
                path,
                GSON.toJson(dto)
        );
    }

    public static List<PlaylistDTO> load()
            throws IOException {

        if (!Files.exists(BASE_DIR)) {
            return List.of();
        }

        try (Stream<Path> paths = Files.list(BASE_DIR)) {

            return paths
                    .filter(p -> p.toString().endsWith(".json"))
                    .map(path -> {

                        try {

                            String json =
                                    Files.readString(path);

                            return GSON.fromJson(
                                    json,
                                    PlaylistDTO.class
                            );

                        } catch (Exception e) {

                            System.err.println(
                                    "Failed loading: " + path
                            );

                            return null;
                        }

                    })
                    .filter(Objects::nonNull)
                    .toList();
        }
    }

    public static void delete(Playlist playlist) throws IOException {
        PlaylistDTO dto = PlaylistMapper.toDTO(playlist);
        Files.delete(BASE_DIR.resolve(dto.title() + ".json"));
    }
}