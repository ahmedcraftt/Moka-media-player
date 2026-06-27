package gui.controllers;

import domain.model.metadata.Metadata;
import domain.model.media.Track;
import infrastructure.media.MetadataManager;
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

    private final SearchEngine searchEngine = SearchEngine.GOOGLE; //will let user change through settings in the future
    @FXML
    private TextArea taLyrics;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnSearch;

    private Track track;
    private MetadataManager metadataManager;
    private String oldLyrics;


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

        taLyrics.setText(data.getLyrics());
        oldLyrics = taLyrics.getText();
    }

    public void handleSave(ActionEvent event) {
        if (track == null) {
            return;
        }

        Metadata metadata = track.getMetadata();

        metadata.setLyrics(taLyrics.getText().trim());

        metadataManager.write(track);

        taLyrics.setEditable(false);
        btnSave.setVisible(false);
        btnEdit.setText("edit");
        oldLyrics = taLyrics.getText();
    }

    public void handelEdit(ActionEvent event) {
        if (btnEdit.getText().equalsIgnoreCase("edit")) {
            taLyrics.setEditable(true);
            btnSave.setVisible(true);
            btnEdit.setText("cancel");
        } else if (btnEdit.getText().equalsIgnoreCase("cancel")) {
            taLyrics.setText(oldLyrics);
            taLyrics.setEditable(false);
            btnSave.setVisible(false);
            btnEdit.setText("edit");
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
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (URISyntaxException e) {
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
}
