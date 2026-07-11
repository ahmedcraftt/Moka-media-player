package gui.controllers;

import application.dto.PlaylistDTO;
import application.service.AppState;
import application.service.PlayerService;
import domain.model.media.Displayable;
import domain.model.media.Playlist;
import domain.model.media.Track;
import gui.controllers.listcells.OpenableDisplayableCell;
import gui.utils.DialogFactory;
import gui.utils.UIContext;
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

    private UIContext uiContext;
    private ViewLoader viewLoader;

    private static Runnable onSaveSuccess;

    private SortByModes currentSortMode = SortByModes.TITLE;

    public void setUIContext(UIContext uiContext) {
        this.uiContext = uiContext;
        loadPlaylists();
        AppState appState = uiContext.appState();
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
        AppState appState = uiContext.appState();
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
        AppState appState = uiContext.appState();
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

        uiContext.playerService().setCurrentList(tracks);
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
        listView.setCellFactory(lv ->
                new OpenableDisplayableCell(uiContext.playerService(), viewLoader, onSaveSuccess));
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
        AppState appState = uiContext.appState();
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
        PlayerService playerService = uiContext.playerService();
        playerService.setSelectTrack(t);
        playerService.playSelectedTrack();
    }

    private void openPlaylist(Playlist p) {
        openList(p.getTracks());
        this.currentPlaylist = p;
    }

    private void openList(List<Track> list) {
        PlayerService playerService = uiContext.playerService();
        ObservableList<Displayable> tracks = FXCollections.observableArrayList();
        tracks.setAll(list != null ? list : List.of());
        if (playerService != null) {
            playerService.setCurrentList(list);
        }
        Platform.runLater(() -> {

            listView.getSelectionModel().clearSelection();
            listView.setItems(tracks);
            uiContext.appState().setCurrentView(tracks);
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
                playlists.add(PlaylistMapper.fromDTO(dto, uiContext.mediaLibrary()));
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
            uiContext.appState().setCurrentView(FXCollections.emptyObservableList());
        });
    }

    private void setupButton(FilterMode mode) {
        List<Track> tracks = uiContext.mediaService().getTracks();
        if (tracks == null) return;
        List<Track> filtered = List.of();
        switch (mode) {
            case FAVORITE -> filtered = tracks.stream().filter(Track::isFavorite).toList();
            case RECENTLY_ADDED ->
                    filtered = tracks.stream().sorted(Comparator.comparing(Track::getDateAdded).reversed()).toList();
            case MOST_PLAYED ->
                    filtered = tracks.stream().sorted(Comparator.comparingInt(Track::getTimesPlayed).reversed()).toList();
        }
        openList(filtered);
    }

    private void createPlaylist() {
        try {
            List<Track> tracks = uiContext.mediaService().getTracks();

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