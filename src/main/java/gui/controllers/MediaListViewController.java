package gui.controllers;

import application.service.PlayerService;
import domain.model.media.Track;

import gui.utils.ImageConverter;
import gui.utils.TimeFormater;
import infrastructure.media.MetadataManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MediaListViewController {

    @FXML private ListView<Track> contentList;
    @FXML private TextField searchBar;
    @FXML private MenuButton btnSort;
    @FXML private Button btnRefresh;
    @FXML
    private Button btnListPlay;

    private List<Track> currentData;
    private PlayerService playerService;
    private static MetadataManager metadataManager;

    public void setPlayerService(PlayerService playerService) {
        if (this.playerService != null) return;
        this.playerService = playerService;
    }

    public void setMetadataManager(MetadataManager metadataManager) {
        MediaListViewController.metadataManager = metadataManager;
    }

    public void inti() {
        sort(SortByModes.TITLE);
    }

    public void setData(List<Track> tracks) {
        this.currentData = tracks;
        contentList.getItems().setAll(tracks);

        if (playerService != null) {
            playerService.setCurrentList(tracks);
        }
    }

    static void loadDataView(Track track) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                MediaListViewController.class.getResource("/views/track-data-view.fxml")
        );

        Parent root = loader.load();
        TrackDataViewController controller = loader.getController();
        controller.setTrack(track);
        controller.setMetadataManager(metadataManager);

        Stage stage = new Stage();
        Image icon = new Image(
                Objects.requireNonNull(
                        MediaListViewController.class.getResourceAsStream("/assets/icons/app-icon.png")
                )
        );
        stage.getIcons().add(icon);
        stage.setTitle("Track info");
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }//loadDataView(Track track)

    @FXML
    protected void initialize() {
        setupListView();
        setupSearch();
        setupPlay();
        setupSort();
        setupRefresh();
    }


    private void sort(SortByModes mode) {
        List<Track> items = new ArrayList<>(contentList.getItems());

        Comparator<Track> baseComparator = switch (mode) {
            case TITLE -> Comparator.comparing(Track::getTitle, String.CASE_INSENSITIVE_ORDER);
            case FILE_NAME -> Comparator.comparing(t -> t.getFiledata().getFileName(), String.CASE_INSENSITIVE_ORDER);
            case ARTISTS -> Comparator.comparing(t -> t.getMediaMetadata().getArtist(), String.CASE_INSENSITIVE_ORDER);
            case DURATION -> Comparator.comparingInt(t -> t.getMetadata().getDurationInSeconds());
            case YEAR -> Comparator.comparingInt(t -> t.getMetadata().getYear().getValue());
            case DATE_ADDED -> Comparator.comparing(Track::getDateAdded);
            case DATE_MODIFIED -> Comparator.comparing(t -> t.getFiledata().getDateModified());
            case DATE_CREATED -> Comparator.comparing(t -> t.getFiledata().getDateCreated());
        };

        Comparator<Track> finalComparator =
                Comparator.comparing((Track t) -> !t.isFavorite())
                        .thenComparing(baseComparator);

        items.sort(finalComparator);
        contentList.getItems().setAll(items);
        btnSort.setText("sort by " + mode);

    }//sort(SortByModes mode)

    @FXML private void sortByTitle() { sort(SortByModes.TITLE); }

    @FXML private void sortByFileName() { sort(SortByModes.FILE_NAME); }

    @FXML private void sortByArtists() { sort(SortByModes.ARTISTS); }

    @FXML private void sortByDuration() { sort(SortByModes.DURATION); }

    @FXML private void sortByYear() { sort(SortByModes.YEAR); }

    @FXML private void sortByDateAdded() { sort(SortByModes.DATE_ADDED); }

    @FXML private void sortByDateModified() { sort(SortByModes.DATE_MODIFIED); }

    @FXML
    private void sortByDateCreated() {
        sort(SortByModes.DATE_CREATED);
    }

    private void setupListView() {
        contentList.setCellFactory(lv -> new MyListCell());
        contentList.setOnMouseClicked(e -> {
            Track selected = contentList.getSelectionModel().getSelectedItem();

            if (selected != null) {
                playerService.setSelectTrack(selected);
            }
            if (e.getClickCount() == 2) {
                playerService.playFromList(selected, contentList.getItems());
            }
        });

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
                playerService.playFromList(playerService.getCurrentList().getFirst(), playerService.getCurrentList());
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
        btnRefresh.setOnAction(e ->
                btnRefresh.fireEvent(new RefreshEvent()));
    }

    private String safe(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private static class MyListCell extends ListCell<Track> {

        private final ImageView artworkView = new ImageView();
        private final Label titleLabel = new Label();
        private final Label artistLabel = new Label();

        private final HBox root = new HBox(10);

        public MyListCell() {

            artworkView.setFitWidth(40);
            artworkView.setFitHeight(40);
            artworkView.setPreserveRatio(true);

            VBox textBox = new VBox(5);
            textBox.getChildren().add(titleLabel);
            textBox.getChildren().add(artistLabel);
            Button infoButton = new Button("⋮");
            root.getChildren().addAll(artworkView, textBox, infoButton);

            infoButton.setOnAction(e -> openTrackInfo());
        }

        private void openTrackInfo() {
            Track track = getItem();
            if (track == null) return;

            try {
                loadDataView(track);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        protected void updateItem(Track item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
                return;
            }

            titleLabel.setText(item.getTitle() + " " + TimeFormater.formatTime(item.getMetadata().getDurationInSeconds()));
            artistLabel.setText(item.getMediaMetadata().getArtist());
            if (item.getMetadata().getArtwork() != null) {
                artworkView.setImage(ImageConverter.convertToImage(item.getMetadata().getArtwork()));
            } else {
                artworkView.setImage(null);
            }

            setGraphic(root);
        }
    }

}