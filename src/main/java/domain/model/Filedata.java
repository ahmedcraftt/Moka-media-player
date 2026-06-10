package domain.model;

import java.nio.file.Path;
import java.time.LocalDate;

public class Filedata {

    private Path filePath;
    private String fileName = "Unknown";
    private LocalDate dateCreated;
    private LocalDate dateModified;
    private LocalDate lastAccessed;
    private String fileType = "Unknown";
    private long fileSize = 0;
    public Filedata() {
    }

    public Path getFilePath() {
        return filePath;
    }

    public String getFilePathString() {
        if (filePath != null) return filePath.toString();
        else return "Unknown";
    }

    public void setFilePath(Path filePath) {
        if (filePath != null)
            this.filePath = filePath;
        else throw new IllegalArgumentException("file path is null");
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        if (fileName != null && !fileName.isBlank())
            this.fileName = fileName;
        else throw new IllegalArgumentException("filename is null or blank");
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public String getDateCreatedString() {
        if (dateCreated != null) return dateCreated.toString();
        else return "Unknown";
    }

    public void setDateCreated(LocalDate dateCreated) {
        if (dateCreated != null)
            this.dateCreated = dateCreated;
        else throw new IllegalArgumentException("date created is null");
    }

    public LocalDate getDateModified() {
        return dateModified;
    }

    public String getDateModifiedString() {
        if (dateModified != null) return dateModified.toString();
        else return "Unknown";
    }

    public void setDateModified(LocalDate dateModified) {
        if (dateModified != null)
            this.dateModified = dateModified;
        else throw new IllegalArgumentException("date modified is null");
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
        if (fileType != null && !fileType.isBlank()) this.fileType = fileType;
        else throw new IllegalArgumentException("file type is null or blank");
    }

    public LocalDate getLastAccessed() {
        return lastAccessed;
    }

    public String getLastAccessedString() {
        if (lastAccessed != null) return lastAccessed.toString();
        else return "Unknown";
    }

    public void setLastAccessed(LocalDate lastAccessed) {
        if (lastAccessed != null)
            this.lastAccessed = lastAccessed;
        else throw new IllegalArgumentException("last accessed is null");
    }

    @Override
    public String toString() {
        return "Filedata{" +
                "dateCreated=" + getDateCreatedString() +
                ", filePath=" + getFilePathString() +
                ", fileName='" + fileName + '\'' +
                ", \ndateModified=" + getDateModifiedString() +
                ", lastAccessed=" + getLastAccessedString() +
                ", fileType='" + fileType + '\'' +
                ", fileSize=" + fileSize +
                '}';
    }
}
