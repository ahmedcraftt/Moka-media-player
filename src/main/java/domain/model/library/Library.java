package domain.model.library;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Library {

    private final String name;
    private List<LibraryFolder> folders;
    private boolean isDefault;

    public Library(String name,
                   List<LibraryFolder> folders,
                   boolean isDefault) {

        this.name = name;
        this.folders = folders;
        this.isDefault = isDefault;
    }

    public String getName() {
        return name;
    }

    public List<LibraryFolder> getFolders() {
        return folders;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean value) {
        this.isDefault = value;
    }

    public List<Path> getRootPaths() {
        return folders.stream()
                .map(LibraryFolder::getPath)
                .toList();
    }

    public void addFolder(LibraryFolder folder) {
        folders.add(folder);
    }

    public void setFolders(ArrayList<LibraryFolder> folders) {
        this.folders = folders;
    }

    public void removeFolder(LibraryFolder folder) {
        if (folder == null) return;

        if (this.folders == null) {
            this.folders = new ArrayList<>();
            return;
        }

        try {
            this.folders.remove(folder);
        } catch (UnsupportedOperationException e) {
            this.folders = new ArrayList<>(this.folders);
            this.folders.remove(folder);
        }
    }
}