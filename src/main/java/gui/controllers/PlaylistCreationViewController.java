package gui.controllers;

import domain.model.Track;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class PlaylistCreationViewController {

    @FXML
    private TextField tfTitle;
    @FXML
    private Label lbTitle;
    @FXML
    private CheckBox cbFavorite;
    @FXML
    private ListView<Track> lvTracks;


}
