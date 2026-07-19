package gui.controllers;

import application.dto.PlaylistDTO;
import application.service.AppState;
import application.service.PlayerService;
import domain.model.media.Displayable;
import domain.model.media.Playlist;
import domain.model.media.Track;
import gui.controllers.listcells.OpenableDisplayableCell;
import gui.utils.DialogFactory;
import gui.main.AppContext;
import gui.utils.ViewLoader;
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
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
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

    private AppContext appContext;

    private ViewLoader viewLoader;

    private Runnable onSaveSuccess;

    private SortByModes currentSortMode = SortByModes.TITLE;

    private FilterMode currentFilterMode = null;

    public void setUIContext(AppContext appContext) {
        this.appContext = appContext;
        loadPlaylists();
        AppState appState = appContext.appState();
        if (viewModes.contains(appState.getCurrentCategoryMode())) {
            appState.clearCurrentView();
        }
        setUpListViewItems();
        appState.setCurrentCategoryMode(ViewMode.PLAYLIST);
    }

    public void setViewLoader(ViewLoader viewLoader) {
        this.viewLoader = viewLoader;
    }

    public void setOnSaveSuccess(Runnable onSaveSuccess) {
        this.onSaveSuccess = onSaveSuccess;
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
        if (currentFilterMode != null) {
            return;
        }
        List<Displayable> visibleItems = listView.getItems();
        if (!visibleItems.isEmpty() && visibleItems.getFirst() instanceof Track) {
            sortCurrentTracks();
        } else {
            applyFilterAndSortPlaylists();
        }
    }

    private void applyFilterAndSortPlaylists() {
        AppState appState = appContext.appState();
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
        if (currentFilterMode != null) return;
        AppState appState = appContext.appState();
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

        appContext.playerService().setCurrentList(tracks);
    }

    @FXML
    private void handleDelete() {
        if (selectedItem == null) return;
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
        listView.setCellFactory(lv ->
                new OpenableDisplayableCell(
                        appContext.playerService(),
                        viewLoader,
                        onSaveSuccess,
                        () -> currentFilterMode
                ));
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
        AppState appState = appContext.appState();
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
        PlayerService playerService = appContext.playerService();
        playerService.setSelectTrack(t);
        playerService.playSelectedTrack();
    }

    private void openPlaylist(Playlist playlist) {
        openList(playlist.getTracks());
        currentFilterMode = null;
        this.currentPlaylist = playlist;
    }

    private void openList(List<Track> list) {
        PlayerService playerService = appContext.playerService();
        ObservableList<Displayable> tracks = FXCollections.observableArrayList();
        tracks.setAll(list != null ? list : List.of());
        playerService.setCurrentList(list);
        Platform.runLater(() -> {
            listView.getSelectionModel().clearSelection();
            listView.setItems(tracks);
            appContext.appState().setCurrentView(tracks);
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
                playlists.add(PlaylistMapper.fromDTO(dto, appContext.mediaLibrary()));
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
        btnFavorites.setOnAction(e -> setupFilterButton(FilterMode.FAVORITE));
        btnRecentlyAdded.setOnAction(e -> setupFilterButton(FilterMode.RECENTLY_ADDED));
        btnMostPlayed.setOnAction(e -> setupFilterButton(FilterMode.MOST_PLAYED));
        btnRecentlyPlayed.setOnAction(e -> setupFilterButton(FilterMode.RECENTLY_PLAYED));
        btnAdd.setOnAction(e -> createPlaylist());
        btnBack.setOnAction(e -> handleBack());
    }

    private void setupFilterButton(FilterMode mode) {
        currentFilterMode = mode;
        List<Track> tracks = appContext.mediaService().getTracks();
        int cutoffDays = appContext.config().getUIConfig().getCutoffDays();
        LocalDateTime cutoff = LocalDateTime.now().minusDays(cutoffDays);

        if (tracks == null) return;
        List<Track> filtered = List.of();
        switch (mode) {
            case FAVORITE -> filtered = tracks.stream()
                    .filter(Track::isFavorite)
                    .sorted(Comparator.comparing(Track::getTitle).reversed())
                    .toList();
            case RECENTLY_ADDED -> filtered = tracks.stream()
                    .sorted(Comparator.comparing(Track::getDateAdded).reversed())
                    .toList();
            case MOST_PLAYED -> filtered = tracks.stream()
                    .filter(track -> track.getTimesPlayed() > 0)
                    .sorted(Comparator.comparingInt(Track::getTimesPlayed).reversed())
                    .toList();
            case RECENTLY_PLAYED -> filtered = tracks.stream()
                    .filter(track -> track.getLastPlayed() != null)
                    .filter(track -> track.getLastPlayed().isAfter(cutoff))
                    .sorted(Comparator.comparing(Track::getLastPlayed).reversed())
                    .toList();
        }
        openList(filtered);
    }

    private void handleBack() {
        if (tfSearchBar != null) {
            tfSearchBar.clear();
        }
        currentFilterMode = null;
        appContext.appState().clearCurrentView();
        applyFilterAndSortPlaylists();
    }

    private void createPlaylist() {
        try {
            List<Track> tracks = appContext.mediaService().getTracks();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/playlist-creation-view.fxml"));
            Parent root = loader.load();

            PlaylistCreationViewController controller = loader.getController();
            controller.setTracks(tracks);

            Image icon = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/assets/icons/app-icon.png")
            ));

            Stage stage = new Stage();
            stage.setTitle("Create playlist");
            stage.setScene(new Scene(root, 720, 580));
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

}