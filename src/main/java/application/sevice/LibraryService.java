package application.sevice;

import infrastructure.storge.LibraryStorage;
import domain.library.Library;
import domain.library.LibraryFolder;

import java.nio.file.Path;
import java.util.*;

public class LibraryService {

    private final List<Library> libraries;
    private Library activeLibrary;

    public LibraryService() {
        this.libraries = LibraryStorage.load();

        if (!libraries.isEmpty()) {
            activeLibrary = libraries.stream()
                    .filter(Library::isDefault)
                    .findFirst()
                    .orElse(libraries.get(0));
        }
    }

    public Library createLibrary(String name, Path path) {

        LibraryFolder folder =
                new LibraryFolder(path, true, true);

        Library library =
                new Library(name, List.of(folder), true);

        addLibrary(library);

        return library;
    }

    public void addLibrary(Library library) {
        libraries.add(library);
        save();
    }

    public void setActiveLibrary(Library library) {
        this.activeLibrary = library;
        save();
    }

    public Library getActiveLibrary() {
        return activeLibrary;
    }

    public List<Library> getLibraries() {
        return libraries;
    }

    public void save() {
        LibraryStorage.save(libraries);
    }

    public boolean hasLibraries() {
        return !libraries.isEmpty();
    }

    public boolean hasActiveLibrary() {
        return activeLibrary != null;
    }
}