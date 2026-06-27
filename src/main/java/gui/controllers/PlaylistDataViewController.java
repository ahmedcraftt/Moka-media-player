package gui.controllers;

import domain.model.media.Playlist;
import domain.model.media.Track;
import gui.utils.ImageConverter;
import gui.utils.TimeFormater;

import infrastructure.storage.PlaylistStorage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class PlaylistDataViewController {
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

    private Playlist playlist;

    private Runnable onSaveSuccessCallback;

    public void setPlaylist(Playlist playlist) {
        if (playlist == null) throw new IllegalArgumentException("Playlist cannot be null");
        this.playlist = playlist;

        imgArtwork.setImage(ImageConverter.convertToImage(playlist.getArtwork()));
        tfTitle.setText(playlist.getTitle());
        cbFavorite.setSelected(playlist.isFavorite());

        syncListViewSelection();
    }

    public void setTracks(List<Track> tracks) {
        lvTracks.getItems().setAll(tracks);
        syncListViewSelection();
    }

    public void setOnSaveSuccess(Runnable callback) {
        this.onSaveSuccessCallback = callback;
    }

    @FXML
    public void initialize() {
        lvTracks.setCellFactory(list -> new MyListCell());
        lvTracks.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void syncListViewSelection() {
        if (playlist == null || lvTracks.getItems().isEmpty()) {
            return;
        }

        SelectionModel<Track> selectionModel = lvTracks.getSelectionModel();
        selectionModel.clearSelection();

        for (int i = 0; i < lvTracks.getItems().size(); i++) {
            if (playlist.contains(lvTracks.getItems().get(i))) {
                selectionModel.select(i);
            }
        }
    }

    @FXML
    public void handleSave(ActionEvent event) {
        if (playlist == null) return;

        playlist.setTitle(tfTitle.getText().trim());
        playlist.setFavorite(cbFavorite.isSelected());

        List<Track> tracks = lvTracks.getSelectionModel().getSelectedItems();
        playlist.clear();
        playlist.addTracks(tracks);

        try {
            PlaylistStorage.save(playlist);
        } catch (IOException e) {
            System.err.println("CRITICAL: Failed to save playlist details to storage file.");
            e.printStackTrace();
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

    private static class MyListCell extends ListCell<Track> {
        private final ImageView artworkView = new ImageView();
        private final Label titleLabel = new Label();
        private final CheckBox selectedCheckBox = new CheckBox();
        private final HBox root = new HBox(10);

        public MyListCell() {
            artworkView.setFitWidth(40);
            artworkView.setFitHeight(40);
            artworkView.setPreserveRatio(true);

            VBox textBox = new VBox(5);
            textBox.getChildren().add(titleLabel);
            textBox.setMaxWidth(150);

            selectedCheckBox.setOnAction(e -> {
                ListView<Track> lv = getListView();
                if (lv == null) return;

                if (selectedCheckBox.isSelected()) {
                    lv.getSelectionModel().select(getIndex());
                } else {
                    lv.getSelectionModel().clearSelection(getIndex());
                }
            });

            root.getChildren().addAll(artworkView, textBox, selectedCheckBox);
        }

        @Override
        protected void updateItem(Track item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
                return;
            }

            titleLabel.setText(item.getTitle() + " [" + TimeFormater.formatTime(item.getMetadata().getDurationInSeconds()) + "]");

            if (item.getMetadata().getArtwork() != null) {
                artworkView.setImage(ImageConverter.convertToImage(item.getMetadata().getArtwork()));
            } else {
                artworkView.setImage(null);
            }

            if (getListView() != null) {
                selectedCheckBox.setSelected(getListView().getSelectionModel().isSelected(getIndex()));
            }

            setGraphic(root);
        }
    }
}