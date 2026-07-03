package application.dto;

import org.jetbrains.annotations.NotNull;

public record TrackDTO(
        int metadataId,
        boolean favorite,
        int timesPlayed,
        String path,
        String type,
        long lastModified,
        long size,
        String dateAdded,
        String dateCreated,
        String lastAccessed,
        String fileType
) {

    @NotNull
    @Override
    public String toString() {
        return "TrackDTO{" +
                "favorite=" + favorite +
                ", timesPlayed=" + timesPlayed +
                ", path='" + path +
                ", type='" + type +
                ", last_modified=" + lastModified +
                ", size=" + size +
                ", dateAdded=" + dateAdded +
                '}';
    }
}
