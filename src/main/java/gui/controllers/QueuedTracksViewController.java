package gui.controllers;

import application.service.PlayerService;
import domain.model.media.Playlist;
import domain.model.media.Track;
import gui.controllers.listcells.QueueTrackCell;
import gui.utils.DialogFactory;
import gui.utils.UIContext;
import gui.utils.ViewLoader;

import infrastructure.storage.PlaylistStorage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;


public class QueuedTracksViewController {

    private static final Logger logger = LoggerFactory.getLogger(QueuedTracksViewController.class);

    @FXML
    private StackPane root;
    @FXML
    private MenuButton mbOptions;
    @FXML
    private MenuItem miSave, miAdd, miClear;
    @FXML
    private ListView<Track> lvQueue;
    @FXML
    private Label lblShuffle, lblNumOf;

    private Runnable onSaveSuccessCallback;
    private ViewLoader viewLoader;
    private UIContext uiContext;

    public void setUIContext(UIContext uiContext) {
        this.uiContext = uiContext;
        init();
    }

    public void setOnSaveSuccessCallback(Runnable onSaveSuccessCallback) {
        this.onSaveSuccessCallback = onSaveSuccessCallback;
    }

    public void setViewLoader(ViewLoader viewLoader) {
        this.viewLoader = viewLoader;
    }

    private void init() {
        setupMenuItems();
        setupLabel();
        setupListView();
        handleShuffling();
    }

    private void setupMenuItems() {
        miSave.setOnAction(this::handleSave);
        miClear.setOnAction(event -> {
            lvQueue.getItems().clear();
            uiContext.player().clearQueue();
        });
        miAdd.setVisible(false);
    }

    private void setupLabel() {
        if (uiContext.player().isShuffle())
            lblShuffle.setText("Shuffled");
        else lblShuffle.setText("Not shuffled");
        int tracks;
        if (uiContext != null)
            tracks = uiContext.player().getNumberOfTracks();
        else tracks = 0;
        lblNumOf.setText(tracks + " Tracks");
    }

    private void handleShuffling() {
        PlayerService playerService = uiContext.playerService();
        playerService.shuffleProperty().addListener((observable, oldValue, newShuffle) -> {
            if (newShuffle) {
                lblShuffle.setText("Shuffled");
            } else lblShuffle.setText("Not Shuffled");

            lvQueue.getItems().clear();
            lvQueue.getItems().addAll(uiContext.player().getQueuedTracks());
        });
    }

    private void setupListView() {
        PlayerService playerService = uiContext.playerService();
        lvQueue.setCellFactory(lv -> new QueueTrackCell(playerService, viewLoader, onSaveSuccessCallback));
        lvQueue.getItems().addAll(uiContext.player().getQueuedTracks());
        lvQueue.setOnMouseClicked(e -> {
            Track selected = lvQueue.getSelectionModel().getSelectedItem();
            playerService.setCurrentList(lvQueue.getItems());
            if (selected != null) {
                playerService.setSelectTrack(selected);
                if (e.getClickCount() == 2) {
                    playerService.playSelectedTrack();
                }
            }
        });
    }

    private void handleSave(ActionEvent event) {
        TextInputDialog saveDialog = DialogFactory.textInputDialog(
                "Save queue as playlist",
                "Enter the playlist title"
        );
        Optional<String> title = saveDialog.showAndWait();
        Playlist newPlaylist = new Playlist(title.get());
        newPlaylist.addTracks(lvQueue.getItems());
        try {
            PlaylistStorage.save(newPlaylist);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
