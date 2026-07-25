package gui.controllers.listcells;

import application.service.PlayerService;
import domain.model.media.Displayable;
import domain.model.media.Playlist;
import domain.model.media.Track;
import gui.models.FilterMode;
import gui.utils.TimeFormatter;
import gui.utils.ViewLoader;
import javafx.scene.control.Button;

import java.io.IOException;
import java.util.function.Supplier;

public class OpenableDisplayableCell extends DisplayableCell {
    private final Button btnInfo = new Button("⋮");
    private final ViewLoader viewLoader;
    private final Runnable onSaveSuccessCallback;
    private Supplier<FilterMode> filterModeSupplier;

    public OpenableDisplayableCell(PlayerService playerService, ViewLoader viewLoader, Runnable onSaveSuccessCallback) {
        this.viewLoader = viewLoader;
        this.onSaveSuccessCallback = onSaveSuccessCallback;
        super(playerService);
        root.setSpacing(10);
        root.getChildren().add(btnInfo);
        artworkView.setFitWidth(40);
        artworkView.setFitHeight(40);
    }

    public OpenableDisplayableCell(
            PlayerService playerService,
            ViewLoader viewLoader,
            Runnable onSaveSuccessCallback,
            Supplier<FilterMode> filterModeSupplier
    ) {
        this(playerService, viewLoader, onSaveSuccessCallback);
        this.filterModeSupplier = filterModeSupplier;
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
            String info = lblInfo.getText();

            FilterMode filterMode = filterModeSupplier.get();

            if (filterModeSupplier == null) {
                lblInfo.setText(info);
            }

            switch (filterMode) {
                case FAVORITE -> lblInfo.setText(info + " ❤");
                case MOST_PLAYED -> lblInfo.setText(info + " [" + track.getTimesPlayed() + "]");
                case RECENTLY_ADDED ->
                        lblInfo.setText(info + " [" + TimeFormatter.formatDate(track.getDateAdded()) + "]");
                case RECENTLY_PLAYED ->
                        lblInfo.setText(info + " [" + TimeFormatter.formatDateTime(track.getLastPlayed()) + "]");
                case null, default -> lblInfo.setText(info);
            }

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
