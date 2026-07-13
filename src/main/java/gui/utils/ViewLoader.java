package gui.utils;

import domain.model.media.Playlist;
import domain.model.media.Track;
import gui.controllers.MediaListViewController;
import gui.controllers.PlaylistDataViewController;
import gui.controllers.TrackDataViewController;
import gui.main.AppContext;
import gui.main.MainApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public final class ViewLoader {

    private static final Logger logger = LoggerFactory.getLogger(ViewLoader.class);

    private final AppContext appContext;

    public ViewLoader(AppContext appContext) {
        this.appContext = appContext;
    }

    public void loadDataView(Track track, Runnable onSaveSuccessCallback) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/track-data-view.fxml"));
        Parent root = loader.load();
        TrackDataViewController controller = loader.getController();
        controller.setUIContext(appContext);
        controller.setTrack(track);
        controller.setOnSaveSuccessCallback(onSaveSuccessCallback);

        Stage stage = new Stage();
        stage.initOwner(MainApplication.primaryStage);
        stage.initModality(Modality.NONE);
        Image icon = new Image(
                Objects.requireNonNull(
                        MediaListViewController.class.getResourceAsStream("/assets/icons/app-icon.png")
                )
        );
        stage.getIcons().add(icon);
        stage.setTitle("Track info");
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }

    public void loadPlaylistDataView(Playlist playlist, Runnable onSaveSuccess) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/playlist-data-view.fxml"));
        Parent root = loader.load();
        PlaylistDataViewController controller = loader.getController();
        controller.setPlaylist(playlist);
        controller.setTracks(appContext.mediaService().getTracks());
        controller.setOnSaveSuccess(onSaveSuccess);

        Stage stage = new Stage();
        stage.initOwner(MainApplication.primaryStage);
        stage.initModality(Modality.NONE);
        Image icon = new Image(Objects.requireNonNull(
                MediaListViewController.class.getResourceAsStream("/assets/icons/app-icon.png")
        ));
        stage.getIcons().add(icon);
        stage.setTitle("Track info");
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }


}
