package gui.controllers;

import domain.model.media.Playlist;
import domain.model.media.Track;
import gui.controllers.listcells.PlaylistListDataCell;
import infrastructure.storage.PlaylistStorage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PlaylistDataViewController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistDataViewController.class);

    @FXML
    private Label lblTitle;
    @FXML
    private TextField tfTitle;
    @FXML
    private CheckBox cbFavorite;
    @FXML
    private ImageView imgArtwork;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;
    @FXML
    private ListView<Track> lvTracks;
    @FXML
    private TextField tfSearch;

    private Playlist playlist;
    private Runnable onSaveSuccessCallback;

    private final ObservableList<Track> masterTrackList = FXCollections.observableArrayList();
    private FilteredList<Track> filteredTrackList;
    private final Set<Track> chosenTracks = new LinkedHashSet<>();

    @FXML
    private void initialize() {
        filteredTrackList = new FilteredList<>(masterTrackList, p -> true);
        lvTracks.setItems(filteredTrackList);

        lvTracks.setCellFactory(list -> new PlaylistListDataCell(chosenTracks));



        lvTracks.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        tfSearch.textProperty().addListener((obs, oldText, newText) -> handleSearch());
    }

    public void setPlaylist(Playlist playlist) {
        if (playlist == null) throw new IllegalArgumentException("Playlist cannot be null");
        this.playlist = playlist;

        tfTitle.setText(playlist.getTitle());
        cbFavorite.setSelected(playlist.isFavorite());

        chosenTracks.clear();
        if (playlist.getTracks() != null) {
            chosenTracks.addAll(playlist.getTracks());
        }

        String artworkPath = null;
        if (playlist.getTracks() != null && !playlist.getTracks().isEmpty()) {
            artworkPath = playlist.getTracks().getFirst().getMetadata().getArtworkPath();
        }

        if (artworkPath != null && !artworkPath.isBlank()) {
            File file = new File(artworkPath);
            if (file.exists()) {
                imgArtwork.setImage(new Image(file.toURI().toString(), true));
            } else {
                loadDefaultHeaderArtwork();
            }
        } else {
            loadDefaultHeaderArtwork();
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

    public void setTracks(List<Track> tracks) {
        masterTrackList.setAll(tracks);
    }

    public void setOnSaveSuccess(Runnable callback) {
        this.onSaveSuccessCallback = callback;
    }

    @FXML
    public void handleSave(ActionEvent event) {
        if (playlist == null) return;

        playlist.setTitle(tfTitle.getText().trim());
        playlist.setFavorite(cbFavorite.isSelected());

        playlist.clear();
        playlist.addTracks(new ArrayList<>(chosenTracks));

        try {
            PlaylistStorage.save(playlist);
        } catch (IOException e) {
            logger.error("CRITICAL: Failed to save playlist details to storage.", e);
        }

        if (onSaveSuccessCallback != null) {
            onSaveSuccessCallback.run();
        }

        closeWindow();
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        if (btnCancel.getScene() != null && btnCancel.getScene().getWindow() != null) {
            Stage stage = (Stage) btnCancel.getScene().getWindow();
            stage.close();
        }
    }

    private void loadDefaultHeaderArtwork() {
        imgArtwork.setImage(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/assets/images/unknown.jpg")).toString(),
                true
        ));
    }

}