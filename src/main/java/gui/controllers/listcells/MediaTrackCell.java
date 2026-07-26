package gui.controllers.listcells;

import application.service.PlayerService;
import gui.utils.ViewLoader;
import javafx.scene.control.Button;

public class MediaTrackCell extends OpenableTrackCell {

    public MediaTrackCell(PlayerService playerService, ViewLoader viewLoader, Runnable onSaveSuccessCallback) {
        super(playerService, viewLoader, onSaveSuccessCallback);
        Button infoButton = new Button("⋮");
        infoButton.getStyleClass().add("cell-menu-button");
        infoButton.setOnAction(e -> openTrackInfo());
        root.getChildren().add(infoButton);
    }

}
