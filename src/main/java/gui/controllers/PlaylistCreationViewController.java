package gui.controllers;

import domain.model.media.Playlist;
import domain.model.media.Track;
import gui.controllers.listcells.PlayListCreationCell;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class PlaylistCreationViewController {

    @FXML
    private TextField tfTitle;
    @FXML
    private Label lbTitle;
    @FXML
    private CheckBox cbFavorite;
    @FXML
    private ListView<Track> lvTracks;
    @FXML
    private Button btnSave;
    @FXML
    private TextField tfSearch;

    private Playlist createdPlaylist;

    private FilteredList<Track> filteredTrackList;

    public void setTracks(List<Track> tracks) {
        lvTracks.getItems().addAll(tracks);
    }

    @FXML
    public void handelSave(ActionEvent event) {
        String title = tfTitle.getText();

        if (title == null || title.isBlank()) {
            return;
        }

        boolean favorite = cbFavorite.isSelected();

        createdPlaylist = new Playlist(title, favorite);
        createdPlaylist.addTracks(
                lvTracks.getSelectionModel().getSelectedItems()
        );

        btnSave.fireEvent(new RefreshEvent());

        Stage stage = (Stage) btnSave.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void handleSearch() {
        String query = tfSearch.getText().toLowerCase().trim();

        filteredTrackList.setPredicate(track -> {
            if (query.isEmpty()) {
                return true;
            }

            return track.getTitle() != null &&
                    track.getTitle().toLowerCase().contains(query);
        });
    }

    public Playlist getCreatedPlaylist() {
        return createdPlaylist;
    }

    @FXML
    public void initialize() {
        filteredTrackList = new FilteredList<>(lvTracks.getItems());
        lvTracks.setCellFactory(list -> new PlayListCreationCell());
        lvTracks.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

}