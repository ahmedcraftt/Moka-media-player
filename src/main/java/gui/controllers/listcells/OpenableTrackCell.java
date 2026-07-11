package gui.controllers.listcells;

import application.service.PlayerService;
import domain.model.media.Track;
import gui.utils.ViewLoader;

public class OpenableTrackCell extends PlayableTrackCell {
    private final ViewLoader viewLoader;
    private final Runnable onSaveSuccessCallback;

    public OpenableTrackCell(PlayerService playerService, ViewLoader viewLoader, Runnable onSaveSuccessCallback) {
        this.viewLoader = viewLoader;
        this.onSaveSuccessCallback = onSaveSuccessCallback;
        super(playerService);
    }

    protected void openTrackInfo() {
        Track track = getItem();
        if (track == null) return;
        try {
            viewLoader.loadDataView(track, onSaveSuccessCallback);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
