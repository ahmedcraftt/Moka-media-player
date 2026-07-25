package config.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public final class ConfigStorage {
    private static final Logger logger = LoggerFactory.getLogger(ConfigStorage.class);

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();
    private static final Path BASE_DIR;

    static {
        if (Boolean.getBoolean("moka.dev")) {
            BASE_DIR = Path.of("configs");
        } else {
            BASE_DIR =
                    Path.of(
                            System.getProperty("user.home"),
                            ".moka_music_player",
                            "configs"
                    );
        }
    }


    private ConfigStorage() {
    }

    public static void save(AppConfig appConfig) {

        try {
            Files.createDirectories(BASE_DIR);

            Path path =
                    BASE_DIR.resolve("config.json");

            Files.write(path, GSON.toJson(appConfig).getBytes());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

    }

    public static AppConfig load() {
        Path path = BASE_DIR.resolve("config.json");

        if (Files.notExists(path)) {
            return new AppConfig();
        }

        String json;
        try {
            json = Files.readString(path);
        } catch (IOException e) {
            logger.error("could not load config {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return GSON.fromJson(json, AppConfig.class);
    }
}
