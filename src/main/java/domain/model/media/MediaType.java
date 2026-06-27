package domain.model.media;

public enum MediaType {
    SONG("song"),
    PODCAST("podcast"),
    AUDIOBOOK("audio book"),
    MUSIC("music"),//not integrated yet
    LECTURE("lecture"),//not integrated yet
    VIDEO("video");//not integrated yet

    private final String title;

    MediaType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static MediaType StringToMediaType(String mediaType) {
        if (mediaType.equalsIgnoreCase(MediaType.AUDIOBOOK.getTitle())
                || mediaType.equalsIgnoreCase("book")) {
            return MediaType.AUDIOBOOK;
        } else if (mediaType.equalsIgnoreCase(MediaType.SONG.getTitle())) {
            return MediaType.SONG;
        } else if (mediaType.equalsIgnoreCase(MediaType.PODCAST.getTitle())) {
            return MediaType.PODCAST;
        } else if (mediaType.equalsIgnoreCase(MediaType.VIDEO.getTitle())) {
            return MediaType.VIDEO;
        } else if (mediaType.equalsIgnoreCase(MediaType.LECTURE.getTitle())) {
            return MediaType.LECTURE;
        } else if (mediaType.equalsIgnoreCase(MediaType.MUSIC.getTitle())) {
            return MediaType.MUSIC;
        } else return MediaType.SONG;//default fallback
    }
}
