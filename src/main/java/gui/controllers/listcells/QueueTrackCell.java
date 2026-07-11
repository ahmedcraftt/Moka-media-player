package gui.controllers.listcells;

import application.service.PlayerService;
import domain.model.media.Track;
import gui.utils.ViewLoader;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

public class QueueTrackCell extends OpenableTrackCell {

    public QueueTrackCell(PlayerService playerService, ViewLoader viewLoader, Runnable onSaveSuccessCallback) {
        super(playerService, viewLoader, onSaveSuccessCallback);
        MenuButton options = new MenuButton("⋮");
        options.getStyleClass().add("menu-button");
        options.getStylesheets().add("/styles/main.css");
        MenuItem info = new MenuItem("ℹ");
        MenuItem remove = new MenuItem("-");
        options.getItems().addAll(info, remove);
        root.getChildren().addAll(options);

        info.setOnAction(e -> openTrackInfo());

        remove.setOnAction(e -> removeFromQueue(getItem()));

    }

    private void removeFromQueue(Track track) {
        playerService.removeTrackFromQueue(track);
    }

}
