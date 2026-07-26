package infrastructure.storage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import domain.model.library.Library;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;

public class LibraryStorage {

    private static final Gson gson = new Gson();
    private static final Path FILE =
            Path.of(System.getProperty("user.home"),
                    ".moka_music_player",
                    "libraries.json");

    private static final Type LIST_TYPE =
            new TypeToken<List<Library>>(){}.getType();

    public static void save(List<Library> libraries) {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, gson.toJson(libraries));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save libraries", e);
        }
    }

    public static List<Library> load() {

        try {

            if (!Files.exists(FILE)) {
                return new ArrayList<>();
            }

            String json = Files.readString(FILE);

            List<Library> libs =
                    gson.fromJson(json, LIST_TYPE);

            if (libs == null) {
                return new ArrayList<>();
            }

            for (Library lib : libs) {
                repairLibrary(lib);
            }

            return libs;

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to load libraries", e
            );
        }
    }

    private static void repairLibrary(Library lib) {

        if (lib.getFolders() == null) {
            lib.setFolders(new ArrayList<>());
        }

        lib.getFolders().removeIf(folder -> folder.getPath() == null);
    }
}

