package ui.controllers;

import entities.Track;

import infrastructure.audio.AudioPlayer;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MediaListViewController {

    @FXML private ListView<Track> contentList;
    @FXML private TextField searchBar;
    @FXML private Button btnListPlay;
    @FXML private MenuButton btnSort;
    @FXML private Button btnRefresh;

    private List<Track> currentData;
    private AudioPlayer player;
    private ControllerServer controllerServer;

    public void setPlayer(AudioPlayer player) {
        this.player = player;
    }

    public void setPlayerService(ControllerServer controllerServer) {
        this.controllerServer = controllerServer;
    }

    public void setData(List<Track> tracks) {
        this.currentData = tracks;
        contentList.getItems().setAll(tracks);
    }

    public void setMode(ViewMode mode) {

        btnListPlay.setText(
                switch (mode) {
                    case BOOKS -> "Read";
                    case PODCASTS -> "Play Episode";
                    case PLAYLISTS -> "Open";
                    default -> "Play";
                }
        );
    }

    @FXML
    private void initialize() {
        setupListView();
        setupSearch();
        setupPlay();
        setupSort();
        setupRefresh();
    }

    private void setupListView() {
        contentList.setCellFactory(lv -> new MyListCell());
        contentList.setOnMouseClicked(e -> {
            Track selected = contentList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                controllerServer.setSelectedTrack(selected);
            }
        });
    }

    private void sort(SortByModes mode) {
        List<Track> items = new ArrayList<>(contentList.getItems());

        switch (mode) {
            case TITLE -> items.sort(Comparator.comparing(track -> track.getMetadata().getTitle(), String.CASE_INSENSITIVE_ORDER));
            case FILE_NAME -> items.sort(Comparator.comparing(track -> track.getFilePath().getFileName().toString()));
            case ARTISTS -> items.sort(Comparator.comparing(track -> track.getMetadata().getArtist(), String.CASE_INSENSITIVE_ORDER));
            case DURATION -> items.sort(Comparator.comparingInt(track -> track.getMetadata().getDurationInSeconds()));
            case YEAR -> items.sort(Comparator.comparing(track -> track.getMetadata().getYear()));
            case DATE_ADDED -> items.sort(Comparator.comparing(Track::getDateCreated));
            case DATE_MODIFIED -> items.sort(Comparator.comparing(t -> t.getFilePath().toFile().lastModified()));
        }

        contentList.getItems().setAll(items);
    }

    @FXML private void sortByTitle() { sort(SortByModes.TITLE); }

    @FXML private void sortByFileName() { sort(SortByModes.FILE_NAME); }

    @FXML private void sortByArtists() { sort(SortByModes.ARTISTS); }

    @FXML private void sortByDuration() { sort(SortByModes.DURATION); }

    @FXML private void sortByYear() { sort(SortByModes.YEAR); }

    @FXML private void sortByDateAdded() { sort(SortByModes.DATE_ADDED); }

    @FXML private void sortByDateModified() { sort(SortByModes.DATE_MODIFIED); }

    private static class MyListCell extends ListCell<Track> {
        @Override
        protected void updateItem(Track item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.getMetadata().getTitle()); // upgrade later with artist etc.
            }
        }
    }

    private void setupSearch() {
        searchBar.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isBlank()) {
                contentList.getItems().setAll(currentData);
            } else {
                String q = newVal.toLowerCase();

                contentList.getItems().setAll(
                        currentData.stream()
                                .filter(t ->
                                        safe(t.getMetadata().getTitle()).contains(q) ||
                                                safe(t.getMetadata().getGenre()).contains(q)
                                )
                                .toList()
                );
            }
        });
    }

    private void setupPlay() {
        btnListPlay.setOnAction(e -> {
            Track first = contentList.getItems().getFirst();

            if (first != null && player != null) {
                player.play(first);
            }

        });
    }

    private void setupSort() {
        btnSort.setOnAction(e -> contentList.getItems().setAll(
                contentList.getItems().stream()
                        .sorted(Comparator.comparing(t -> safe(t.getMetadata().getTitle())))
                        .toList()
        ));
    }

    private void setupRefresh() {
        btnRefresh.setOnAction(e -> contentList.getItems().setAll(currentData));
    }

    private String safe(String s) {
        return s == null ? "" : s.toLowerCase();
    }
}