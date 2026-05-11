package gui.controllers;

import domain.model.Playlist;
import infrastructure.audio.AudioPlayer;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.util.List;

public class CategoryViewController {

    @FXML
    private ListView contentList;

    private AudioPlayer player;

    private List<Playlist> currentData;

    public void setPlayer(AudioPlayer player) {
        this.player = player;
    }

    public void setData(List<Playlist> categoryList) {

        this.currentData = categoryList;

        contentList.getItems().setAll(categoryList);

    }
}
