package gui.controllers;

import domain.model.media.Playlist;
import domain.model.media.Track;
import gui.utils.TimeFormater;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Objects;

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

    private Playlist createdPlaylist;

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

    public Playlist getCreatedPlaylist() {
        return createdPlaylist;
    }

    @FXML
    public void initialize() {
        lvTracks.setCellFactory(list -> new MyListCell());
        lvTracks.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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

            titleLabel.setText(item.getTitle() + " " + TimeFormater.formatTime(item.getMetadata().getDurationInSeconds()));

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