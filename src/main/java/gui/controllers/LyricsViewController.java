package gui.controllers;

import domain.model.metadata.Metadata;
import domain.model.media.Track;
import gui.main.AppContext;
import gui.model.SearchEngine;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import platform.OS;
import platform.OSDetector;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import platform.UnSupportedOSException;

import java.awt.Desktop;
import java.io.File;
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
    private Button btnSave, btnEdit, btnLyrics, btnAlignment;
    @FXML
    private ContextMenu cxmMenu, cxmLyrics;
    @FXML
    private MenuItem miLeft, miRight, miCenter, miSearch, miProvideFile;

    private SwitchMode currentSwitchMode = SwitchMode.VIEW;

    private final Text txtLyrics = new Text();

    private Track track;
    private AppContext appContext;
    private String oldLyrics;

    private SearchEngine searchEngine;

    public void setTrack(Track track) {
        this.track = track;

        if (track == null) {
            return;
        }

        loadTrackLyrics();
    }

    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
        tflLyricsView.setTextAlignment(appContext.config().getUIConfig().getLyricsTextAlignment());
        searchEngine = appContext.config().getSearchConfig().getPreferredSearchEngine();
        handleSave();
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

    public void handleProvidedFile(ActionEvent event) {
        if (track != null) Platform.runLater(this::pickFile);
    }

    public void searchWeb(String query) {

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        String url = "";

        switch (searchEngine) {
            case GOOGLE -> url = "https://www.google.com/search?q=" + encodedQuery;
            case DUCK_DUCK_GO -> url = "https://duckduckgo.com/?q=" + encodedQuery;
            case BING -> url = "https://www.bing.com/search?q=" + encodedQuery;
            case BRAVE -> url = "https://search.brave.com/search?q=" + encodedQuery;
        }

        String finalUrl = url;

        OS os = OSDetector.getOS();

        if (os == OS.WINDOWS || os == OS.MAC) {
            Platform.runLater(() -> {
                try {
                    Desktop.getDesktop().browse(new URI(finalUrl));
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        } else if (os == OS.LINUX) {
            CompletableFuture.runAsync(() -> {
                try {
                    new ProcessBuilder("xdg-open", finalUrl).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else throw new UnSupportedOSException("OS is not supported");
    }

    private void pickFile() {
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Select a File");

        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Lyrics Files", "*.lrc", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showOpenDialog(btnLyrics.getScene().getWindow());

        if (file == null) return;

        appContext.lyricsEmbedder().embedLyrics(track, file.toPath());
    }

    private void handleLyricsBtn() {
        btnLyrics.setOnAction(event ->
                cxmLyrics.show(btnLyrics, Side.BOTTOM, 0, 0));
    }

    private void handleAlignmentBtn() {
        btnAlignment.setOnAction(event ->
                cxmMenu.show(btnAlignment, Side.BOTTOM, 0, 0));
    }

    private void handleMenuOptions() {
        miLeft.setOnAction(event -> tflLyricsView.setTextAlignment(TextAlignment.LEFT));
        miCenter.setOnAction(event -> tflLyricsView.setTextAlignment(TextAlignment.CENTER));
        miRight.setOnAction(event -> tflLyricsView.setTextAlignment(TextAlignment.RIGHT));
    }

    private void loadTrackLyrics() {

        Metadata data = track.getMetadata();

        txtLyrics.setText(data.getLyrics());

        taLyricsEditor.setText(data.getLyrics());

        oldLyrics = data.getLyrics();
    }

    private void handleSave() {
        btnSave.setOnAction(event -> {
            if (track == null) {
                return;
            }

            Metadata metadata = track.getMetadata();

            metadata.setLyrics(taLyricsEditor.getText().trim());

            appContext.metadataManager().write(track);

            txtLyrics.setText(metadata.getLyrics());

            oldLyrics = metadata.getLyrics();

            switchToViewMode();
        });
    }

    @FXML
    private void initialize() {
        setupLyricsText();
        handleAlignmentBtn();
        handleLyricsBtn();
        handleMenuOptions();
    }

    private void setupLyricsText() {
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

