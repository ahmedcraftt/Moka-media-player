package gui.controllers.listcells;

import application.service.PlayerService;

import domain.model.media.Track;
import gui.utils.ViewLoader;

import java.util.logging.Logger;

public class OpenableTrackCell extends PlayableTrackCell {
    private static final Logger logger = Logger.getLogger(String.valueOf(OpenableTrackCell.class));
    private final ViewLoader viewLoader;
    private final Runnable onSaveSuccessCallback;

    public OpenableTrackCell(PlayerService playerService, ViewLoader viewLoader, Runnable onSaveSuccessCallback) {
        this.viewLoader = viewLoader;
        this.onSaveSuccessCallback = onSaveSuccessCallback;
        super(playerService);
    }

    protected void openTrackInfo() {
        Track track = getItem();
        if (track == null) {
            logger.warning("Track is null");
            return;
        }
        try {
            viewLoader.loadDataView(track, onSaveSuccessCallback);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
