package ui.main;

import application.LibraryService;
import application.MediaService;
import infrastructure.audio.AudioPlayer;
import infrastructure.media.JaudiotaggerManager;
import infrastructure.media.MediaScanner;
import infrastructure.media.MetaDataManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mediaLibrary.MediaLibrary;
import ui.controllers.MainViewController;
import ui.controllers.ControllerServer;


import java.io.IOException;

public class MainApplication extends Application {
    private final AudioPlayer player = new AudioPlayer();
    private final MetaDataManager metaDataManger = new JaudiotaggerManager();
    private final MediaScanner scanner = new MediaScanner(metaDataManger);
    private final MediaLibrary library = new MediaLibrary();
    private final LibraryService libraryService = new LibraryService();
    private final MediaService mediaService = new MediaService(scanner,library,libraryService);
    private final ControllerServer controllerServer = new ControllerServer();
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/mainView.fxml"));
        Parent root = loader.load();
            MainViewController controller = loader.getController();
            controller.setPlayer(player);
            controller.setPlayerService(controllerServer);
            controller.setMediaService(mediaService);
            controller.setLibraryService(libraryService);

        Scene scene = new Scene(root,1000,750);

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()){
                case P ->{
                    if (controllerServer.getSelectedTrack()!=null) {
                        player.play(controllerServer.getSelectedTrack());
                    }
                }
                case SPACE -> player.pause();
                case RIGHT -> player.playNext();
                case LEFT -> player.playPrev();
            }
        });

        stage.setTitle("Moka Player ☕");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setOnCloseRequest(_ -> System.out.println("Closing app..."));
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
