package gui.controllers;

import application.service.MediaService;
import application.service.PlayerService;
import domain.model.media.Track;
import gui.utils.TimeFormater;
import infrastructure.media.MetadataManager;
import infrastructure.storage.MetadataStorage;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MediaListViewController {

    @FXML
    private AnchorPane mainRoot;
    @FXML private ListView<Track> contentList;
    @FXML private TextField searchBar;
    @FXML private MenuButton btnSort;
    @FXML private Button btnRefresh;
    @FXML
    private Button btnListPlay;
    @FXML
    private CheckMenuItem cmiAscending;

    private List<Track> currentData = new ArrayList<>();
    private PlayerService playerService;
    private static MetadataManager metadataManager;
    private static MetadataStorage metadataStorage;
    private static MediaService mediaService;

    private SortByModes currentSortMode = SortByModes.TITLE;

    public void setPlayerService(PlayerService playerService) {
        if (this.playerService != null) return;
        this.playerService = playerService;
    }

    public void setMetadataManager(MetadataManager metadataManager) {
        MediaListViewController.metadataManager = metadataManager;
    }

    public void setMetadataStorage(MetadataStorage metadataStorage) {
        MediaListViewController.metadataStorage = metadataStorage;
    }

    public void setMediaService(MediaService mediaService) {
        MediaListViewController.mediaService = mediaService;
    }

    public void inti() {
        sort(SortByModes.TITLE);
    }

    public void setData(List<Track> tracks) {
        this.currentData = tracks != null ? tracks : new ArrayList<>();
        applyFilterAndSort();
    }

    @FXML
    protected void initialize() {
        setupListView();
        setupSearch();
        setupPlay();
        setupSort();
        setupRefresh();
    }

    private void sort(SortByModes mode) {
        this.currentSortMode = mode;
        if (btnSort != null) {
            btnSort.setText("sort by " + mode.toString().toLowerCase());
        }
        applyFilterAndSort();
    }

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

    private void applyFilterAndSort() {
        if (currentData == null) return;

        String query = (searchBar != null && searchBar.getText() != null)
                ? searchBar.getText().toLowerCase().trim()
                : "";

        List<Track> processedList = currentData.stream()
                .filter(t -> {
                    if (query.isEmpty()) return true;

                    String trackTitle = safe(t.getTitle());
                    String metaTitle = t.getMetadata() != null ? safe(t.getMetadata().getTitle()) : "";
                    String metaGenre = t.getMetadata() != null ? safe(t.getMetadata().getGenre()) : "";
                    String metaArtist = t.getMetadata() != null ? safe(t.getMetadata().getArtist()) : "";

                    return trackTitle.contains(query) ||
                            metaTitle.contains(query) ||
                            metaGenre.contains(query) ||
                            metaArtist.contains(query);
                })
                .toList();

        List<Track> workingList = new ArrayList<>(processedList);

        Comparator<Track> baseComparator = switch (currentSortMode) {
            case TITLE -> Comparator.comparing(t -> safe(t.getTitle()), String.CASE_INSENSITIVE_ORDER);
            case FILE_NAME -> Comparator.comparing(t -> safe(t.getFileName()), String.CASE_INSENSITIVE_ORDER);
            case ARTISTS ->
                    Comparator.comparing(t -> t.getMetadata() != null ? safe(t.getMetadata().getArtist()) : "", String.CASE_INSENSITIVE_ORDER);
            case DURATION ->
                    Comparator.comparingInt(t -> t.getMetadata() != null ? t.getMetadata().getDurationInSeconds() : 0);
            case YEAR ->
                    Comparator.comparingInt(t -> (t.getMetadata() != null && t.getMetadata().getYear() != null) ? t.getMetadata().getYear().getValue() : 0);
            case DATE_ADDED ->
                    Comparator.comparing(Track::getDateAdded, Comparator.nullsLast(Comparator.naturalOrder()));
            case DATE_MODIFIED ->
                    Comparator.comparing(t -> t.getFiledata() != null ? t.getFiledata().getDateModified() : null, Comparator.nullsLast(Comparator.naturalOrder()));
            case DATE_CREATED ->
                    Comparator.comparing(t -> t.getFiledata() != null ? t.getFiledata().getDateCreated() : null, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> throw new IllegalStateException("Unexpected value: " + currentSortMode);
        };

        if (cmiAscending != null && !cmiAscending.isSelected()) baseComparator = baseComparator.reversed();

        Comparator<Track> hierarchicalComparator = Comparator.comparing((Track t) -> !t.isFavorite())
                .thenComparing(baseComparator);

        workingList.sort(hierarchicalComparator);

        contentList.getItems().setAll(workingList);

        if (playerService != null) {
            playerService.setCurrentList(workingList);
        }
    }

    private void setupListView() {
        contentList.setCellFactory(lv -> new MyListCell());
        contentList.setOnMouseClicked(e -> {
            Track selected = contentList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                playerService.setSelectTrack(selected);
                if (e.getClickCount() == 2) {
                    playerService.playFromList(selected, contentList.getItems());
                }
            }
        });
    }

    private void setupSearch() {
        if (searchBar != null) {
            searchBar.textProperty().addListener((obs, oldVal, newVal) -> applyFilterAndSort());
        }
    }

    private void setupSort() {
        if (cmiAscending != null) {
            cmiAscending.setOnAction(e -> applyFilterAndSort());
        }
    }

    private void setupPlay() {
        btnListPlay.setOnAction(e -> {
            List<Track> visibleItems = contentList.getItems();
            if (!visibleItems.isEmpty() && playerService != null) {
                playerService.playFromList(visibleItems.getFirst(), visibleItems);
            }
        });
    }

    private void setupRefresh() {
        btnRefresh.setOnAction(e -> btnRefresh.fireEvent(new RefreshEvent()));
    }

    private String safe(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    static void loadDataView(Track track) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                MediaListViewController.class.getResource("/views/track-data-view.fxml")
        );

        Parent root = loader.load();
        TrackDataViewController controller = loader.getController();
        controller.setTrack(track);
        controller.setMetadataManager(metadataManager);
        controller.setStorage(metadataStorage);
        controller.setMediaService(mediaService);

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
    }
    
    private static class MyListCell extends ListCell<Track> {
        private final ImageView artworkView = new ImageView();
        private final Label titleLabel = new Label();
        private final Label artistLabel = new Label();
        private final HBox root = new HBox(10);

        private static Image defaultArtwork;

        public MyListCell() {
            if (defaultArtwork == null) {
                try {
                    defaultArtwork = new Image(
                            Objects.requireNonNull(MyListCell.class.getResourceAsStream("/assets/images/unknown.jpg")),
                            40, 40, true, true
                    );
                } catch (Exception e) {
                    System.err.println("Default cell artwork asset not found.");
                }
            }

            artworkView.setFitWidth(40);
            artworkView.setFitHeight(40);
            artworkView.setPreserveRatio(true);

            VBox textBox = new VBox(5);
            textBox.getChildren().addAll(titleLabel, artistLabel);

            HBox.setHgrow(textBox, Priority.ALWAYS);

            Button infoButton = new Button("⋮");
            infoButton.getStyleClass().add("cell-menu-button");
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

            long duration = (item.getMetadata() != null) ? item.getMetadata().getDurationInSeconds() : 0;
            String displayTitle = item.getTitle() != null ? item.getTitle() : "Unknown Track";
            titleLabel.setText(displayTitle + " " + TimeFormater.formatTime(duration));

            String artist = (item.getMetadata() != null && item.getMetadata().getArtist() != null)
                    ? item.getMetadata().getArtist()
                    : "Unknown Artist";
            artistLabel.setText(artist);

            String artworkPath = (item.getMetadata() != null) ? item.getMetadata().getArtworkPath() : null;

            if (artworkPath != null && !artworkPath.isBlank()) {
                File file = new File(artworkPath);
                if (file.exists()) {
                    artworkView.setImage(new Image(file.toURI().toString(), 40, 40, true, true, true));
                } else {
                    artworkView.setImage(defaultArtwork);
                }
            } else {
                artworkView.setImage(defaultArtwork);
            }

            setGraphic(root);
        }
    }
}