package domain.model;

public enum MediaType {
    SONG("song"),
    PODCAST("podcast"),
    AUDIOBOOK("audio book"),
    MUSIC("music"),
    LECTURE("lecture"), //not integrated yet
    VIDEO("video");//not integrated yet

    private final String title;

    MediaType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static MediaType StringToMediaType(String mediaType) {
        if (mediaType.equalsIgnoreCase(MediaType.AUDIOBOOK.getTitle())) {
            return MediaType.AUDIOBOOK;
        } else if (mediaType.equalsIgnoreCase(MediaType.SONG.getTitle())) {
            return MediaType.SONG;
        } else if (mediaType.equalsIgnoreCase(MediaType.PODCAST.getTitle())) {
            return MediaType.PODCAST;
        } else return MediaType.SONG;
    }
}
