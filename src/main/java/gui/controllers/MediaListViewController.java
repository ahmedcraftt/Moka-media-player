package gui.controllers;

import application.sevice.PlayerService;
import domain.model.Track;

import gui.utils.FXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
    private PlayerService playerService;

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }


    @FXML
    private void initialize() {
        setupListView();
        setupSearch();
        setupPlay();
        setupSort();
        setupRefresh();
    }


    private void sort(SortByModes mode) {
        List<Track> items = new ArrayList<>(contentList.getItems());

        switch (mode) {
            case TITLE -> items.sort(Comparator.comparing(Track::getTitle, String.CASE_INSENSITIVE_ORDER));
            case FILE_NAME -> items.sort(Comparator.comparing(track -> track.getFilePath().getFileName().toString()));
            case ARTISTS -> items.sort(Comparator.comparing(track -> track.getMetadata().getArtist(), String.CASE_INSENSITIVE_ORDER));
            case DURATION -> items.sort(Comparator.comparingInt(track -> track.getMetadata().getDurationInSeconds()));
            case YEAR -> items.sort(Comparator.comparing(track -> track.getMetadata().getYear()));
            case DATE_ADDED -> items.sort(Comparator.comparing(track -> track.getFileData().getDateCreated()));
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


    private void setupListView() {
        contentList.setCellFactory(lv -> new MyListCell());
        contentList.setOnMouseClicked(e -> {
            Track selected = contentList.getSelectionModel().getSelectedItem();

            if (selected != null) {
                playerService.setCurrentTrack(selected);
            }
            if (e.getClickCount() == 2) {
                playerService.playFromList(selected, contentList.getItems());
            }
        });

    }

    private static class MyListCell extends ListCell<Track> {

        private final ImageView artworkView = new ImageView();
        private final Label titleLabel = new Label();
        private final Button infoButton = new Button("⋮");

        private final HBox root = new HBox(10);
        private final VBox textBox = new VBox(5);

        public MyListCell() {
            artworkView.setFitWidth(40);
            artworkView.setFitHeight(40);
            artworkView.setPreserveRatio(true);

            textBox.getChildren().addAll(titleLabel);
            root.getChildren().addAll(artworkView, textBox, infoButton);

            infoButton.setOnAction(e -> {
                Track track = getItem();
                if (track != null) {
                    System.out.println("Info: " + track.getTitle());
                    // TODO: open side panel here
                }
            });
        }

        @Override
        protected void updateItem(Track item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
                return;
            }

            titleLabel.setText(item.getTitle() + " " + item.getDuration());

            if (item.getMetadata().getArtwork() != null) {
                artworkView.setImage(FXUtils.convertToImage(item.getMetadata().getArtwork()));
            } else {
                artworkView.setImage(null);
            }

            setGraphic(root);
        }
    }

    public void setData(List<Track> tracks) {
        this.currentData = tracks;
        contentList.getItems().setAll(tracks);

        if (playerService != null) {
            playerService.setCurrentList(tracks);
        }
    }

    public void setMode(ViewMode mode) {

        btnListPlay.setText(
                switch (mode) {
                    case BOOKS -> "Read";
                    case PODCASTS -> "Play Episode";
                    default -> "Play";
                }
        );
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
            if (!contentList.getItems().isEmpty()) {
                playerService.playFromList(playerService.getCurrentTrack(), playerService.getCurrentList());
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