package gui.controllers;

import application.dto.PlaylistDTO;
import application.service.AppState;
import application.service.PlayerService;
import domain.model.library.MediaLibrary;
import domain.model.media.Displayable;
import domain.model.media.Playlist;
import domain.model.media.Track;
import gui.utils.DialogFactory;
import gui.utils.TimeFormater;
import infrastructure.mapper.PlaylistMapper;
import infrastructure.storage.PlaylistStorage;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PlaylistViewController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistViewController.class);

    @FXML
    private ListView<Displayable> listView;
    @FXML
    private TextField tfSearchBar;
    @FXML
    private Button btnBack, btnAdd, btnDelete, btnFavorites, btnRecentlyAdded, btnMostPlayed, btnRecentlyPlayed;
    @FXML
    private MenuButton btnSort;
    @FXML
    private MenuItem miTitle, miNumOfTracks, miFavorite;
    @FXML
    private CheckMenuItem cmiAscending;

    private final Set<ViewMode> viewModes = new HashSet<>(Set.of(ViewMode.ALBUM, ViewMode.ARTISTS, ViewMode.GENRE));

    private final ObservableList<Playlist> playlists = FXCollections.observableArrayList();
    private final ObservableList<Displayable> filteredDisplayables = FXCollections.observableArrayList();

    private Displayable selectedItem;

    private Playlist currentPlaylist;

    private MediaLibrary mediaLibrary;
    private PlayerService playerService;
    private AppState appState;

    private static List<Track> tracksList;
    private static Runnable onSaveSuccess;

    private SortByModes currentSortMode = SortByModes.TITLE;

    public void setMediaLibrary(MediaLibrary mediaLibrary) {
        this.mediaLibrary = mediaLibrary;
        loadPlaylists();
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public void setAppState(AppState appState) {
        this.appState = appState;
        if (viewModes.contains(appState.getCurrentCategoryMode())) {
            appState.clearCurrentView();
        }
        setUpListViewItems();
        appState.setCurrentCategoryMode(ViewMode.PLAYLIST);
    }

    public void setTracksList(List<Track> tracksList) {
        PlaylistViewController.tracksList = tracksList;
    }

    public void setOnSaveSuccess(Runnable onSaveSuccess) {
        PlaylistViewController.onSaveSuccess = onSaveSuccess;
    }

    @FXML
    public void initialize() {
        setUpListView();
        setupSearch();
        setupButtons();
        setUpSort();
    }

    private void setUpSort() {
        miTitle.setOnAction(e -> changeSortMode(SortByModes.TITLE));
        miNumOfTracks.setOnAction(e -> changeSortMode(SortByModes.NUM_OF_TRACKS));
        miFavorite.setOnAction(e -> changeSortMode(SortByModes.FAVORITE));

        if (cmiAscending != null) {
            cmiAscending.setOnAction(e -> triggerSortPipeline());
        }
    }

    private void changeSortMode(SortByModes mode) {
        this.currentSortMode = mode;
        if (btnSort != null) {
            btnSort.setText("sort by: " + mode.toString().toLowerCase().replace("_", " "));
        }
        triggerSortPipeline();
    }

    private void triggerSortPipeline() {
        List<Displayable> visibleItems = listView.getItems();
        if (!visibleItems.isEmpty() && visibleItems.getFirst() instanceof Track) {
            sortCurrentTracks();
        } else {
            applyFilterAndSortPlaylists();
        }
    }

    private void applyFilterAndSortPlaylists() {
        String query = (tfSearchBar != null && tfSearchBar.getText() != null)
                ? tfSearchBar.getText().toLowerCase().trim()
                : "";

        Comparator<Playlist> playlistComparator = switch (currentSortMode) {
            case TITLE -> Comparator.comparing(Playlist::getTitle, String.CASE_INSENSITIVE_ORDER);
            case NUM_OF_TRACKS -> Comparator.comparingInt(p -> p.getTracks() != null ? p.getTracks().size() : 0);
            case FAVORITE -> Comparator.comparing((Playlist p) -> !p.isFavorite());
            default -> throw new IllegalStateException("Unexpected value: " + currentSortMode);
        };

        if (cmiAscending != null && !cmiAscending.isSelected()) {
            playlistComparator = playlistComparator.reversed();
        }

        List<Playlist> processed = playlists.stream()
                .filter(p -> query.isEmpty() || p.getTitle().toLowerCase().contains(query))
                .sorted(playlistComparator)
                .toList();

        filteredDisplayables.setAll(processed);

        if (appState != null && (appState.getCurrentView() == null || appState.getCurrentView().isEmpty() || listView.getItems() == filteredDisplayables)) {
            listView.setItems(filteredDisplayables);
        }
    }

    private void sortCurrentTracks() {
        List<Track> tracks = listView.getItems().stream()
                .filter(Track.class::isInstance)
                .map(Track.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));

        if (tracks.isEmpty()) return;

        Comparator<Track> trackComparator = switch (currentSortMode) {
            case TITLE -> Comparator.comparing(t -> t.getTitle().trim() != null ? t.getTitle() :
                    "", String.CASE_INSENSITIVE_ORDER);
            case FAVORITE -> Comparator.comparing((Track t) -> !t.isFavorite());
            default ->
                    Comparator.comparing(t -> t.getTitle() != null ? t.getTitle() : "", String.CASE_INSENSITIVE_ORDER);
        };

        if (cmiAscending != null && !cmiAscending.isSelected()) {
            trackComparator = trackComparator.reversed();
        }

        tracks.sort(trackComparator);

        ObservableList<Displayable> updatedTrackView = FXCollections.observableArrayList(tracks);
        listView.setItems(updatedTrackView);

        if (appState != null) {
            appState.setCurrentView(updatedTrackView);
        }
        if (playerService != null) {
            playerService.setCurrentList(tracks);
        }
    }

    @FXML
    private void handleDelete() {
        switch (selectedItem) {
            case Track track -> {
                listView.getItems().remove(selectedItem);
                currentPlaylist.removeTrack(track);
            }
            case Playlist playlist -> {
                Alert alert = DialogFactory.confirmation(
                        "Delete Playlist?",
                        "Delete this playlist",
                        "Are you sure you want to delete this playlist?"
                );

                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    listView.getItems().remove(selectedItem);
                    try {
                        PlaylistStorage.delete(playlist);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }

            }
            default -> throw new IllegalStateException("Unexpected value: " + selectedItem);
        }
    }

    private void setUpListView() {
        listView.setCellFactory(lv -> new MyListCell());
        listView.setOnMouseClicked(event -> {
            Displayable item = listView.getSelectionModel().getSelectedItem();
            if (item != null) {
                handleItemClick(item);
                if (event.getClickCount() >= 2) {
                    handleItemDoubleClick(item);
                }
            }
        });
    }

    private void setUpListViewItems() {
        if (appState.getCurrentView().isEmpty()) {
            listView.setItems(filteredDisplayables);
        } else {
            listView.setItems(appState.getCurrentView());
        }
    }

    private void handleItemDoubleClick(Displayable item) {
        if (item instanceof Playlist p) {
            openPlaylist(p);
        } else if (item instanceof Track t) {
            playTrack(t);
        }
    }

    private void handleItemClick(Displayable item) {
        this.selectedItem = item;
    }

    private void playTrack(Track t) {
        playerService.setSelectTrack(t);
        playerService.playSelectedTrack();
    }

    private void openPlaylist(Playlist p) {
        openList(p.getTracks());
        this.currentPlaylist = p;
    }

    private void openList(List<Track> list) {
        ObservableList<Displayable> tracks = FXCollections.observableArrayList();
        tracks.setAll(list != null ? list : List.of());
        if (playerService != null) {
            playerService.setCurrentList(list);
        }
        Platform.runLater(() -> {
            listView.getSelectionModel().clearSelection();
            listView.setItems(tracks);
            if (appState != null) {
                appState.setCurrentView(tracks);
            }
            if (!tracks.isEmpty()) {
                sortCurrentTracks();
            }
        });
    }

    private void loadPlaylists() {
        try {
            playlists.clear();
            List<PlaylistDTO> dtos = PlaylistStorage.load();

            for (PlaylistDTO dto : dtos) {
                playlists.add(PlaylistMapper.fromDTO(dto, mediaLibrary));
            }

            applyFilterAndSortPlaylists();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load playlists");
        }
    }

    private void setupSearch() {
        if (tfSearchBar != null) {
            tfSearchBar.textProperty().addListener((obs, oldVal, newVal) -> applyFilterAndSortPlaylists());
        }
    }

    private void setupButtons() {
        btnFavorites.setOnAction(e -> setupButton(FilterMode.FAVORITE));
        btnRecentlyAdded.setOnAction(e -> setupButton(FilterMode.RECENTLY_ADDED));
        btnMostPlayed.setOnAction(e -> setupButton(FilterMode.MOST_PLAYED));
        btnRecentlyPlayed.setVisible(false);
        btnAdd.setOnAction(e -> createPlaylist());

        btnBack.setOnAction(event -> {
            if (tfSearchBar != null) {
                tfSearchBar.clear();
            }
            applyFilterAndSortPlaylists();
            if (appState != null) {
                appState.setCurrentView(FXCollections.emptyObservableList());
            }
        });
    }

    private void setupButton(FilterMode mode) {
        if (tracksList == null) return;
        List<Track> filtered = List.of();
        switch (mode) {
            case FAVORITE -> filtered = tracksList.stream().filter(Track::isFavorite).toList();
            case RECENTLY_ADDED ->
                    filtered = tracksList.stream().sorted(Comparator.comparing(Track::getDateAdded).reversed()).toList();
            case MOST_PLAYED ->
                    filtered = tracksList.stream().sorted(Comparator.comparingInt(Track::getTimesPlayed).reversed()).toList();
        }
        openList(filtered);
    }

    private void createPlaylist() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/playlist-creation-view.fxml"));
            Parent root = loader.load();

            PlaylistCreationViewController controller = loader.getController();
            controller.setTracks(tracksList);

            Image icon = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/assets/icons/app-icon.png")
            ));

            Stage stage = new Stage();
            stage.setTitle("Create playlist");
            stage.setScene(new Scene(root));
            stage.getIcons().add(icon);
            stage.showAndWait();

            Playlist playlist = controller.getCreatedPlaylist();
            if (playlist != null) {
                playlists.add(playlist);
                applyFilterAndSortPlaylists();
                PlaylistStorage.save(playlist);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            showError("Failed to create playlist");
        }
    }

    private void showError(String msg) {
        Alert alert = DialogFactory.error("Error", null, msg);
        alert.showAndWait();
    }

    private static void loadPlaylistDataView(Playlist playlist) throws IOException {
        FXMLLoader loader = new FXMLLoader(PlaylistViewController.class.getResource("/views/playlist-data-view.fxml"));
        Parent root = loader.load();
        PlaylistDataViewController controller = loader.getController();
        controller.setPlaylist(playlist);
        controller.setTracks(tracksList);
        controller.setOnSaveSuccess(onSaveSuccess);

        Stage stage = new Stage();
        Image icon = new Image(Objects.requireNonNull(
                MediaListViewController.class.getResourceAsStream("/assets/icons/app-icon.png")
        ));
        stage.getIcons().add(icon);
        stage.setTitle("Track info");
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }

    private static class MyListCell extends ListCell<Displayable> {

        private final Label title = new Label();
        private final Label info = new Label();
        private final ImageView artworkView = new ImageView();
        private final Button btnInfo = new Button("⋮");
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
                    System.err.println("Fallback cell asset path missing.");
                }
            }

            artworkView.setFitWidth(40);
            artworkView.setFitHeight(40);
            artworkView.setPreserveRatio(true);

            VBox textBox = new VBox(5);
            textBox.getChildren().addAll(title, info);

            HBox.setHgrow(textBox, Priority.ALWAYS);

            root.getChildren().addAll(artworkView, textBox, btnInfo);
        }

        @Override
        protected void updateItem(Displayable item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
                return;
            }

            String artworkPath = item.getArtworkPath();

            if (item instanceof Track track) {
                title.setText(track.getTitle());
                info.setText(TimeFormater.formatTime(track.getMetadata().getDurationInSeconds())
                        + " " + track.getMetadata().getArtist());

                btnInfo.setOnAction(event -> {
                    try {
                        MediaListViewController.loadDataView(track);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

            } else if (item instanceof Playlist playlist) {
                title.setText(playlist.getTitle() + " d " +
                        playlist.size() + " Tracks " +
                        TimeFormater.formatTime(playlist.getTotalDurationSeconds())
                );

                if (playlist.getTracks() != null && !playlist.getTracks().isEmpty()) {
                    info.setText("First track: " + playlist.getTracks().getFirst().getTitle());
                } else {
                    info.setText("Empty Playlist");
                }

                btnInfo.setOnAction(event -> {
                    try {
                        loadPlaylistDataView(playlist);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

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