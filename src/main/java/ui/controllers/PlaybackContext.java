package ui.controllers;

import entities.Track;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.List;

public class PlaybackContext {

    private final ObjectProperty<Track> selectedTrack = new SimpleObjectProperty<>();
    private final ObjectProperty<List<Track>> currentList = new SimpleObjectProperty<>();

    public Track getSelectedTrack() {
        return selectedTrack.get();
    }

    public void setSelectedTrack(Track track) {
        selectedTrack.set(track);
    }

    public List<Track> getCurrentList() {
        return currentList.get();
    }

    public void setCurrentList(List<Track> list) {
        currentList.set(list);
    }
}