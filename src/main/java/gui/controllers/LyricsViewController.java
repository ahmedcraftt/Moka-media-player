package gui.controllers;

import domain.model.metadata.Metadata;
import domain.model.media.Track;
import infrastructure.media.MetadataManager;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import platform.OS;
import platform.OSDetector;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class LyricsViewController {

    @FXML
    private ScrollPane spLyricsContainer;

    @FXML
    private TextArea taLyricsEditor;

    @FXML
    private TextFlow tflLyricsView;

    @FXML
    private Button btnSave, btnEdit, btnSearch;

    private SwitchMode currentSwitchMode = SwitchMode.VIEW;

    private final Text txtLyrics = new Text();

    private Track track;
    private MetadataManager metadataManager;
    private String oldLyrics;

    private final SearchEngine searchEngine = SearchEngine.GOOGLE;

    public void setTrack(Track track) {
        this.track = track;

        if (track == null) {
            return;
        }

        loadTrackLyrics();
    }

    public void setMetadataManager(MetadataManager metadataManager) {
        this.metadataManager = metadataManager;
    }

    private void loadTrackLyrics() {

        Metadata data = track.getMetadata();

        txtLyrics.setText(data.getLyrics());

        taLyricsEditor.setText(data.getLyrics());

        oldLyrics = data.getLyrics();
    }

    public void handleSave(ActionEvent event) {
        if (track == null) {
            return;
        }

        Metadata metadata = track.getMetadata();

        metadata.setLyrics(taLyricsEditor.getText().trim());

        metadataManager.write(track);

        txtLyrics.setText(metadata.getLyrics());

        oldLyrics = metadata.getLyrics();

        switchToViewMode();

    }

    public void handelEdit(ActionEvent event) {
        switch (currentSwitchMode) {
            case VIEW -> switchToEditMode();
            case EDIT -> {
                taLyricsEditor.setText(oldLyrics);
                switchToViewMode();
            }
        }
    }

    public void handelSearch(ActionEvent event) {
        if (track != null) searchWeb("lyrics:" + track.getTitle());
    }

    public void searchWeb(String query) {

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        String url = "";

        switch (searchEngine) {
            case GOOGLE -> url = "https://www.google.com/search?q=" + encodedQuery;
            case DUCK_DUCK_GO -> url = "https://duckduckgo.com/?q=" + encodedQuery;
            case BING -> url = "https://www.bing.com/search?q=" + encodedQuery;
        }

        String finalUrl = url;

        if (OSDetector.getOS() == OS.WINDOWS || OSDetector.getOS() == OS.MAC) {
            Platform.runLater(() -> {
                try {
                    Desktop.getDesktop().browse(new URI(finalUrl));
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        } else if (OSDetector.getOS() == OS.LINUX) {
            CompletableFuture.runAsync(() -> {
                try {
                    new ProcessBuilder("xdg-open", finalUrl).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

    }

    @FXML
    private void initialize() {
        tflLyricsView.getChildren().setAll(txtLyrics);

        txtLyrics.getStyleClass().add("lyrics-text");

        txtLyrics.wrappingWidthProperty().bind(
                tflLyricsView.widthProperty().subtract(24)
        );

        tflLyricsView.setVisible(true);
        tflLyricsView.setManaged(true);

        taLyricsEditor.setVisible(false);
        taLyricsEditor.setManaged(false);
    }

    private void switchToEditMode() {
        currentSwitchMode = SwitchMode.EDIT;
        taLyricsEditor.setEditable(true);
        btnSave.setVisible(true);
        btnEdit.setText("Cancel");

        taLyricsEditor.setTranslateX(30);
        taLyricsEditor.setOpacity(0);

        taLyricsEditor.setVisible(true);
        taLyricsEditor.setManaged(true);

        editModeTransaction();
    }

    private void editModeTransaction() {
        TranslateTransition out = new TranslateTransition(Duration.millis(250), tflLyricsView);
        out.setFromX(0);
        out.setToX(-30);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), tflLyricsView);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        TranslateTransition in = new TranslateTransition(Duration.millis(250), taLyricsEditor);
        in.setFromX(30);
        in.setToX(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), taLyricsEditor);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition transition =
                new ParallelTransition(out, fadeOut, in, fadeIn);

        transition.setOnFinished(e -> {
            tflLyricsView.setVisible(false);
            tflLyricsView.setManaged(false);
            taLyricsEditor.requestFocus();
        });

        transition.play();
    }

    private void switchToViewMode() {
        currentSwitchMode = SwitchMode.VIEW;
        taLyricsEditor.setEditable(false);
        btnSave.setVisible(false);
        btnEdit.setText("edit");

        taLyricsEditor.setVisible(false);
        taLyricsEditor.setManaged(false);

        tflLyricsView.setVisible(true);
        tflLyricsView.setManaged(true);

        viewModeTransaction();

        spLyricsContainer.setEffect(new DropShadow(
                BlurType.GAUSSIAN,
                Color.rgb(200, 155, 109, 0.25),
                12,
                0.3,
                0,
                0
        ));
    }

    private void viewModeTransaction() {
        tflLyricsView.setTranslateX(30);
        tflLyricsView.setOpacity(0);

        tflLyricsView.setVisible(true);
        tflLyricsView.setManaged(true);

        TranslateTransition out = new TranslateTransition(Duration.millis(250), taLyricsEditor);
        out.setFromX(0);
        out.setToX(-30);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), taLyricsEditor);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        TranslateTransition in = new TranslateTransition(Duration.millis(250), tflLyricsView);
        in.setFromX(30);
        in.setToX(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), tflLyricsView);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition transition =
                new ParallelTransition(out, fadeOut, in, fadeIn);

        transition.setOnFinished(e -> {
            taLyricsEditor.setVisible(false);
            taLyricsEditor.setManaged(false);
        });

        transition.play();
    }

    private enum SwitchMode {
        EDIT, VIEW
    }

}
