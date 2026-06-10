package domain.library;

import java.nio.file.Path;

public class LibraryFolder {

    private final String path;
    private boolean recursive;
    private boolean enabled;

    public LibraryFolder(Path path,
                         boolean recursive,
                         boolean enabled) {

        this.path = path.toString();
        this.recursive = recursive;
        this.enabled = enabled;
    }

    public Path getPath() {
        return Path.of(path);
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}