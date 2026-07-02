package gui.controllers;

import domain.model.media.Playlist;
import domain.model.media.Track;
import gui.utils.TimeFormater;
import infrastructure.storage.PlaylistStorage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

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

        tfTitle.setText(playlist.getTitle());
        cbFavorite.setSelected(playlist.isFavorite());

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

        syncListViewSelection();
    }

    private void loadDefaultHeaderArtwork() {
        imgArtwork.setImage(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/assets/images/unknown.jpg")).toString(),
                true
        ));
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

        private static Image defaultArtwork;

        public MyListCell() {
            if (defaultArtwork == null) {
                try {
                    defaultArtwork = new Image(
                            Objects.requireNonNull(MyListCell.class.getResourceAsStream("/assets/images/unknown.jpg")),
                            40, 40, true, true
                    );
                } catch (Exception e) {
                    System.err.println("Fallback cell asset path missing.");
                }
            }

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

            String artworkPath = item.getMetadata().getArtworkPath();

            if (artworkPath != null && !artworkPath.isBlank()) {
                File file = new File(artworkPath);
                if (file.exists()) {
                    artworkView.setImage(new Image(file.toURI().toString(), 40, 40, true, true, true));
                } else {
                    artworkView.setImage(defaultArtwork);
                }
            } else {
                artworkView.setImage(defaultArtwork);
            }

            if (getListView() != null) {
                selectedCheckBox.setSelected(getListView().getSelectionModel().isSelected(getIndex()));
            }

            setGraphic(root);
        }
    }
}