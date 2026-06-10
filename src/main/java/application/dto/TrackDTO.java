package application.dto;

public record TrackDTO(
        String title,
        boolean favorite,
        int timesPlayed,
        String path,
        String type,
        long lastModified,
        long size,
        String dateAdded
) {

    @Override
    public String toString() {
        return "TrackDTO{" +
                "favorite=" + favorite +
                ", title='" + title +
                ", timesPlayed=" + timesPlayed +
                ", path='" + path +
                ", type='" + type +
                ", last_modified=" + lastModified +
                ", size=" + size +
                ", dateAdded=" + dateAdded +
                '}';
    }
}
