package ui.main;

import application.LibraryService;
import application.MediaService;
import infrastructure.audio.AudioEngine;
import infrastructure.audio.AudioPlayer;
import infrastructure.audio.VLCJAudioEngine;
import infrastructure.media.JaudiotaggerManager;
import infrastructure.media.MediaScanner;
import infrastructure.media.MetaDataManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import mediaLibrary.MediaLibrary;
import ui.controllers.MainViewController;
import ui.controllers.SelectionModel;


import java.io.IOException;

public class MainApplication extends Application {
    private final AudioEngine engine = new VLCJAudioEngine();
    private final AudioPlayer player = new AudioPlayer(engine);
    private final MetaDataManager metaDataManger = new JaudiotaggerManager();
    private final MediaScanner scanner = new MediaScanner(metaDataManger);
    private final MediaLibrary library = new MediaLibrary();
    private final LibraryService libraryService = new LibraryService();
    private final MediaService mediaService = new MediaService(scanner,library,libraryService);
    private final SelectionModel selectionModel = new SelectionModel();
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/mainView.fxml"));

        Parent root = loader.load();

        MainViewController controller = loader.getController();

        controller.setPlayer(player);
        controller.setSelectionModel(selectionModel);
        controller.setMediaService(mediaService);
        controller.setLibraryService(libraryService);

        Scene scene = new Scene(root,1000,750);

        setUpKeyboardInput(scene);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F11) stage.setFullScreen(!stage.isFullScreen());
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

    private void setUpKeyboardInput(Scene scene){
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()){
                case P ->{
                    if (selectionModel.getSelectedTrack()!=null&&selectionModel.getCurrentList()!=null) {
                        player.playFromList(selectionModel.getSelectedTrack(),selectionModel.getCurrentList());
                    }
                }
                case SPACE -> player.pause();
                case RIGHT -> player.playNext();
                case LEFT -> player.playPrev();
                case M -> player.setVolume(0);
                case UP -> player.setVolume(10);
                case DOWN -> player.setVolume(-10);

            }
        });
    }
}
