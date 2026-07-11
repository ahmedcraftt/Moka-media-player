package gui.controllers.listcells;

import application.service.PlayerService;
import domain.model.media.Displayable;
import domain.model.media.Playlist;
import domain.model.media.Track;
import gui.utils.ViewLoader;
import javafx.scene.control.Button;

import java.io.IOException;

public class OpenableDisplayableCell extends DisplayableCell {
    private final Button btnInfo = new Button("⋮");
    private final ViewLoader viewLoader;
    private final Runnable onSaveSuccessCallback;

    public OpenableDisplayableCell(PlayerService playerService, ViewLoader viewLoader, Runnable onSaveSuccessCallback) {
        this.viewLoader = viewLoader;
        this.onSaveSuccessCallback = onSaveSuccessCallback;
        super(playerService);
        root.setSpacing(10);
        root.getChildren().add(btnInfo);
        artworkView.setFitWidth(40);
        artworkView.setFitHeight(40);
    }

    @Override
    protected void updateItem(Displayable item, boolean empty) {
        super.updateItem(item, empty);

        if (item instanceof Track track) {
            btnInfo.setOnAction(event -> {
                try {
                    viewLoader.loadDataView(track, onSaveSuccessCallback);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (item instanceof Playlist playlist) {
            btnInfo.setOnAction(event -> {
                try {
                    viewLoader.loadPlaylistDataView(playlist, onSaveSuccessCallback);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
