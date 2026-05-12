package domain.model;

import java.nio.file.Path;
import java.time.LocalDate;

public class Filedata {

    private Path filePath;
    private String fileName;
    private LocalDate dateCreated;
    private LocalDate dateModified;
    private LocalDate lastAccessed;
    private String fileType;
    private long fileSize;

    public Filedata(Path filePath, String fileName) {
        this.filePath = filePath;
        this.fileName = fileName;
    }

    public Filedata() {
    }

    public Path getFilePath() {
        return filePath;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LocalDate getDateModified() {
        return dateModified;
    }

    public void setDateModified(LocalDate dateModified) {
        this.dateModified = dateModified;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public LocalDate getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(LocalDate lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    @Override
    public String toString() {
        return "Filedata{" +
                "dateCreated=" + dateCreated +
                ", filePath=" + filePath +
                ", fileName='" + fileName + '\'' +
                ", \ndateModified=" + dateModified +
                ", lastAccessed=" + lastAccessed +
                ", fileType='" + fileType + '\'' +
                ", fileSize=" + fileSize +
                '}';
    }
}
