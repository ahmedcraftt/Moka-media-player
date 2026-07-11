package gui.controllers;

import application.service.AppState;
import application.service.PlayerService;
import domain.model.media.Displayable;
import domain.model.media.Playlist;
import domain.model.media.Track;
import gui.controllers.listcells.DisplayableCell;

import gui.utils.UIContext;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.*;
import java.util.stream.Collectors;

public class CategoryViewController {

    @FXML
    private ListView<Displayable> contentList;

    @FXML
    private MenuButton btnSort;
    @FXML
    private MenuItem miAtoZ, miNumOfTracks;
    @FXML
    private CheckMenuItem cmiAscending;
    @FXML
    private TextField txtSearch;

    private final ObservableList<Playlist> categories = FXCollections.observableArrayList();
    private final ObservableList<Displayable> filteredCategories = FXCollections.observableArrayList();

    private UIContext uiContext;

    private SortByModes currentSortMode = SortByModes.ALPHABETICAL;
    private List<Playlist> rawData;

    public void setUiContext(UIContext uiContext) {
        this.uiContext = uiContext;
        AppState appState = uiContext.appState();
        if (appState.getCurrentCategoryMode() == ViewMode.PLAYLIST) {
            appState.clearCurrentView();
        }
    }

    public void setData(List<Playlist> categoryPlaylists, ViewMode mode) {
        AppState appState = uiContext.appState();

        this.rawData = categoryPlaylists;
        if (categoryPlaylists != null) {
            this.categories.setAll(categoryPlaylists);
        }

        if (appState != null) {
            ViewMode previousMode = appState.getCurrentCategoryMode();

            if (previousMode != null && previousMode != mode || previousMode == ViewMode.PLAYLIST) {
                if (txtSearch != null) {
                    txtSearch.clear();
                }
                appState.setCurrentView(FXCollections.emptyObservableList());
                if (contentList != null) {
                    contentList.getSelectionModel().clearSelection();
                }
            }

            appState.setCurrentCategoryMode(mode);

        }


        syncListViewItems();

        applySearchAndSort();
    }

    @FXML
    public void initialize() {
        setUpListView();
        setupSearch();
        setupContextActions();

        contentList.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        handleBackNavigation();
                        event.consume();
                    }
                });
            }
        });

    }

    private void syncListViewItems() {
        AppState appState = uiContext.appState();
        if (contentList == null || appState == null) return;

        if (appState.getCurrentView() == null || appState.getCurrentView().isEmpty()) {
            contentList.setItems(filteredCategories);
        } else {
            contentList.setItems(appState.getCurrentView());
        }
    }

    private void setUpListView() {
        if (contentList != null) {
            contentList.setCellFactory(lv -> new DisplayableCell(uiContext.playerService()));
            contentList.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> {
                        if (newVal != null) {
                            handleItemClick(newVal);
                        }
                    }
            );
        }
    }

    private void handleItemClick(Displayable item) {
        if (item instanceof Playlist p) {
            openCategoryPlaylist(p);
        } else if (item instanceof Track t) {
            playTrack(t);
        }
    }

    private void playTrack(Track t) {
        PlayerService playerService = uiContext.playerService();
        if (playerService != null) {
            playerService.setSelectTrack(t);
            playerService.playSelectedTrack();
        }
    }

    private void openCategoryPlaylist(Playlist p) {
        PlayerService playerService = uiContext.playerService();
        AppState appState = uiContext.appState();
        if (p != null && p.getTracks() != null) {
            ObservableList<Displayable> tracks = FXCollections.observableArrayList(p.getTracks());
            if (playerService != null) {
                playerService.setCurrentList(p.getTracks());
            }
            Platform.runLater(() -> {
                contentList.getSelectionModel().clearSelection();
                contentList.setItems(tracks);
                if (appState != null) {
                    appState.setCurrentView(tracks);
                }
            });
        }
    }

    private void setupSearch() {
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applySearchAndSort());
        }
    }

    private void setupContextActions() {
        if (miAtoZ != null) miAtoZ.setOnAction(e -> changeSortMode(SortByModes.ALPHABETICAL));
        if (miNumOfTracks != null) miNumOfTracks.setOnAction(e -> changeSortMode(SortByModes.NUM_OF_TRACKS));
        if (cmiAscending != null) cmiAscending.setOnAction(e -> applySearchAndSort());
    }

    private void changeSortMode(SortByModes mode) {
        this.currentSortMode = mode;
        applySearchAndSort();
    }

    private void applySearchAndSort() {
        if (contentList == null || rawData == null) return;

        String query = (txtSearch != null && txtSearch.getText() != null)
                ? txtSearch.getText().toLowerCase().trim()
                : "";

        List<Playlist> processedList = categories.stream()
                .filter(playlist -> {
                    if (query.isEmpty()) return true;
                    String title = playlist.getTitle() != null ? playlist.getTitle().toLowerCase() : "";
                    return title.contains(query);
                })
                .collect(Collectors.toList());

        Comparator<Playlist> comparator = switch (currentSortMode) {
            case ALPHABETICAL -> Comparator.comparing(
                    p -> p.getTitle() != null ? p.getTitle() : "",
                    String.CASE_INSENSITIVE_ORDER
            );
            case NUM_OF_TRACKS -> Comparator.comparingInt(Playlist::size);
            default -> throw new IllegalStateException("Unexpected value: " + currentSortMode);
        };

        if (cmiAscending != null && !cmiAscending.isSelected()) {
            comparator = comparator.reversed();
        }

        processedList.sort(comparator);

        final List<Playlist> finalDataList = processedList;
        Platform.runLater(() -> filteredCategories.setAll(finalDataList));
    }

    public void handleBackNavigation() {
        AppState appState = uiContext.appState();
        if (txtSearch != null) {
            txtSearch.clear();
        }
        Platform.runLater(() -> {
            if (contentList != null && appState != null) {
                appState.setCurrentView(FXCollections.emptyObservableList());

                contentList.setItems(filteredCategories);
                appState.setCurrentView(contentList.getItems());
            }
        });
    }

}