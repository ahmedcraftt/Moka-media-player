package gui.controllers;

import domain.model.media.Playlist;
import domain.model.media.Track;

import gui.utils.ImageConverter;
import gui.utils.TimeFormater;
import infrastructure.factory.PlaylistFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

        createdPlaylist = PlaylistFactory.create(title, favorite);

        createdPlaylist.addTracks(
                lvTracks.getSelectionModel().getSelectedItems()
        );

        btnSave.fireEvent(new RefreshEvent());

        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    public Playlist getCreatedPlaylist() {
        return createdPlaylist;
    }


    @FXML
    public void initialize() {
        lvTracks.setCellFactory(list -> new MyListCell());

        lvTracks.getSelectionModel()
                .setSelectionMode(SelectionMode.MULTIPLE);
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
                if (selectedCheckBox.isSelected()) {
                    getListView().getSelectionModel().select(getIndex());
                } else {
                    getListView().getSelectionModel().clearSelection(getIndex());
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

            if (item.getMetadata().getArtwork() != null) {
                artworkView.setImage(ImageConverter.convertToImage(item.getMetadata().getArtwork()));
            } else {
                artworkView.setImage(null);
            }

            setGraphic(root);
        }

    }
}
