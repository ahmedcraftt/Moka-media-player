package gui.controllers;

import domain.model.Track;
import infrastructure.audio.AudioPlayer;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class PlaylistViewController {
    @FXML private ListView<Track> listView;
    @FXML private TextField searchBar;

    private AudioPlayer player;

    public void setPlayer(AudioPlayer player) {
        this.player = player;
    }

}
