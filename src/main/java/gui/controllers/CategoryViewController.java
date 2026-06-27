package gui.controllers;

import domain.model.media.Track;
import infrastructure.audio.AudioPlayer;
import gui.utils.ImageConverter;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class CategoryViewController {

    @FXML
    private ListView<List<Track>> contentList;

    private AudioPlayer player;
    private List<List<Track>> currentData;
    private ViewMode viewMode;

    public void setPlayer(AudioPlayer player) {
        this.player = player;
    }

    public void setData(List<List<Track>> categoryList) {
        this.currentData = categoryList;
        if (contentList != null && categoryList != null) {
            contentList.getItems().setAll(categoryList);
        }
    }

    public void setViewMode(ViewMode viewMode) {
        this.viewMode = viewMode;
    }

    @FXML
    public void initialize() {
        contentList.setCellFactory(listView -> new CategoryListCell());

        contentList.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                List<Track> selectedCategory = contentList.getSelectionModel().getSelectedItem();
                if (selectedCategory != null && player != null) {
                    player.enqueueAll(selectedCategory);
                }
            }
        });

        if (currentData != null) {
            contentList.getItems().setAll(currentData);
        }
    }

    private class CategoryListCell extends ListCell<List<Track>> {
        private final ImageView artworkView = new ImageView();
        private final Label titleLabel = new Label();
        private final Label countLabel = new Label();
        private final HBox root = new HBox(15);

        public CategoryListCell() {
            artworkView.setFitWidth(50);
            artworkView.setFitHeight(50);
            artworkView.setPreserveRatio(true);

            VBox textBox = new VBox(4, titleLabel, countLabel);
            root.getChildren().addAll(artworkView, textBox);
            root.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        }

        @Override
        protected void updateItem(List<Track> item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null || item.isEmpty()) {
                setGraphic(null);
                return;
            }

            Track representativeTrack = item.get(0);
            String displayTitle = "Unknown Category";

            if (viewMode != null && representativeTrack.getMetadata() != null) {
                displayTitle = switch (viewMode) {
                    case ALBUM -> representativeTrack.getMediaMetadata().getSeries();
                    case ARTISTS -> representativeTrack.getMediaMetadata().getArtist();
                    case GENRE -> representativeTrack.getMetadata().getGenre();
                    default -> "Unknown";
                };
            }

            if (displayTitle == null || displayTitle.trim().isEmpty()) {
                displayTitle = "Unknown";
            }

            titleLabel.setText(displayTitle);

            int totalTracks = item.size();
            countLabel.setText(totalTracks + " " + (totalTracks == 1 ? "track" : "tracks"));

            if (representativeTrack.getMetadata() != null && representativeTrack.getMetadata().getArtwork() != null) {
                artworkView.setImage(ImageConverter.convertToImage(representativeTrack.getMetadata().getArtwork()));
            } else {
                artworkView.setImage(null);
            }

            setGraphic(root);
        }
    }
}