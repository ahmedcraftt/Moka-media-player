package gui.controllers;

public enum SortByModes {

    TITLE("title"),
    FILE_NAME("file name"),
    ARTISTS("artist"),
    DURATION("duration"),
    YEAR("year"),
    DATE_CREATED("created"),
    DATE_ADDED("added"),
    DATE_MODIFIED("modified"),
    ALPHABETICAL("a-z"),
    NUM_OF_TRACKS("No. of tracks"),
    FAVORITE("favorite");

    private final String text;

    SortByModes(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
