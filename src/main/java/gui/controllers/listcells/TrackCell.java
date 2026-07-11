package gui.controllers.listcells;

import domain.model.media.Track;
import domain.model.metadata.Metadata;
import gui.utils.ArtworkCache;
import gui.utils.TimeFormater;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class TrackCell extends ListCell<Track> {
    protected final ImageView artworkView = new ImageView();
    protected final Label lblTitle = new Label();
    protected final Label lblArtist = new Label();
    protected final HBox root = new HBox(10);

    protected static final double MAX_WIDTH = 500;

    protected static Image defaultArtwork;

    public TrackCell() {

        if (getDefaultArtwork() == null) {
            try {
                setDefaultArtwork(new Image(
                        Objects.requireNonNull(getClass().getResourceAsStream("/assets/images/unknown.jpg")),
                        40, 40, true, true
                ));
            } catch (Exception e) {
                System.err.println("Default cell artwork asset not found.");
            }
        }

        artworkView.setFitWidth(40);
        artworkView.setFitHeight(40);
        artworkView.setPreserveRatio(true);

        getLblTitle().setMaxWidth(getMaxWidth());

        VBox textBox = new VBox(5);
        textBox.getChildren().addAll(getLblTitle(), getLblArtist());

        HBox.setHgrow(textBox, Priority.ALWAYS);

        getRoot().getChildren().addAll(artworkView, textBox);

    }

    @Override
    protected void updateItem(Track item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            return;
        }

        Metadata metadata = item.getMetadata();

        long duration = (metadata != null) ? metadata.getDurationInSeconds() : 0;
        String displayTitle = item.getTitle() != null ? item.getTitle() : "Unknown Track";
        getLblTitle().setText(displayTitle);

        String artist = (metadata != null && metadata.getArtist() != null)
                ? metadata.getArtist()
                : "Unknown Artist";
        getLblArtist().setText(TimeFormater.formatTime(duration) + " • " + artist);

        if (metadata != null) {
            artworkView.setImage(
                    ArtworkCache.get(metadata.getArtworkPath())
            );
        } else artworkView.setImage(defaultArtwork);

        setGraphic(getRoot());
    }

    public static Image getDefaultArtwork() {
        return defaultArtwork;
    }

    public static void setDefaultArtwork(Image defaultArtwork) {
        TrackCell.defaultArtwork = defaultArtwork;
    }

    public Label getLblTitle() {
        return lblTitle;
    }

    public Label getLblArtist() {
        return lblArtist;
    }

    public HBox getRoot() {
        return root;
    }

}
