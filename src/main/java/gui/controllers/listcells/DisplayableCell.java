package gui.controllers.listcells;

import application.service.PlayerService;
import domain.model.media.Displayable;
import domain.model.media.Playlist;
import domain.model.media.Track;
import domain.model.metadata.Metadata;
import gui.controllers.FilterMode;
import gui.utils.ArtworkCache;
import gui.utils.TimeFormatter;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class DisplayableCell extends ListCell<Displayable> {
    protected final ImageView artworkView = new ImageView();
    protected final Label lblTitle = new Label();
    protected final Label lblInfo = new Label();
    protected final HBox root = new HBox(10);

    protected static final PseudoClass CURRENT_TRACK = PseudoClass.getPseudoClass("current-track");

    protected static final double MAX_WIDTH = 500;

    protected static Image defaultArtwork;

    private final PlayerService playerService;
    private FilterMode filterMode;

    public DisplayableCell(PlayerService playerService) {
        this.playerService = playerService;
        super();

        if (defaultArtwork == null) {
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

        getLblTitle().setMaxWidth(MAX_WIDTH);

        VBox textBox = new VBox(5);
        textBox.getChildren().addAll(lblTitle, lblInfo);

        HBox.setHgrow(textBox, Priority.ALWAYS);

        root.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(artworkView, textBox);

        playerService.currentTrackProperty().addListener(
                (obs, oldTrack, newTrack)
                        -> updateHighlight());
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

    public HBox getRoot() {
        return root;
    }

    @Override
    protected void updateItem(Displayable item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        String artworkPath = item.getArtworkPath();

        if (item instanceof Track track) {
            updateHighlight();

            Metadata metadata = track.getMetadata();

            long duration = (metadata != null) ? metadata.getDurationInSeconds() : 0;
            String displayTitle = item.getTitle() != null ? item.getTitle() : "Unknown";
            getLblTitle().setText(displayTitle);

            String artist = (metadata != null && metadata.getArtist() != null)
                    ? metadata.getArtist()
                    : "Unknown artist";
            String info = TimeFormatter.formatDuration(duration) + " • " + artist;
            lblTitle.setText(displayTitle);

            lblInfo.setText(info);

        } else if (item instanceof Playlist playlist) {
            int totalTracks = playlist.size();

            lblTitle.setText(playlist.getTitle() + " " +
                    totalTracks + " " + (totalTracks == 1 ? "track" : "tracks " +
                    TimeFormatter.formatDuration(playlist.getTotalDurationSeconds()))
            );

            if (playlist.getTracks() != null && !playlist.getTracks().isEmpty()) {
                lblInfo.setText("First track: " + playlist.getTracks().getFirst().getTitle());
            } else {
                lblInfo.setText("Empty");
            }
        }

        if (artworkPath != null && !artworkPath.isBlank()) {
            artworkView.setImage(ArtworkCache.get(artworkPath));
        } else {
            artworkView.setImage(defaultArtwork);
        }

        setGraphic(root);

    }

    private void updateHighlight() {
        if (getItem() instanceof Track track) {

            pseudoClassStateChanged(
                    CURRENT_TRACK,
                    track != null && track.equals(playerService.getCurrentTrack())
            );
        }
    }

}
