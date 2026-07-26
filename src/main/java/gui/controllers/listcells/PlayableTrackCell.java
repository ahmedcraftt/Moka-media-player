package gui.controllers.listcells;

import application.service.PlayerService;
import domain.model.media.Track;
import javafx.css.PseudoClass;

public class PlayableTrackCell extends TrackCell {
    protected static final PseudoClass CURRENT_TRACK = PseudoClass.getPseudoClass("current-track");
    protected final PlayerService playerService;

    public PlayableTrackCell(PlayerService playerService) {
        this.playerService = playerService;
        super();
        playerService.currentTrackProperty().addListener(
                (obs, oldTrack, newTrack)
                        -> updateHighlight());
    }

    @Override
    protected void updateItem(Track item, boolean empty) {
        super.updateItem(item, empty);
        updateHighlight();
    }

    private void updateHighlight() {
        Track item = getItem();

        pseudoClassStateChanged(
                CURRENT_TRACK,
                item != null && item.equals(playerService.getCurrentTrack())
        );
    }
}
