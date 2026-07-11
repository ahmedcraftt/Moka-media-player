package gui.controllers;

import application.service.AppState;
import application.service.PlayerService;
import domain.model.media.Track;
import gui.controllers.listcells.MediaTrackCell;
import gui.utils.UIContext;

import gui.utils.ViewLoader;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.util.*;


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

    private static Runnable onSaveSuccessCallback;

    private ViewLoader viewLoader;
    private UIContext uiContext;

    private SortByModes currentSortMode;

    public void setOnSaveSuccessCallback(Runnable onSaveSuccessCallback) {
        MediaListViewController.onSaveSuccessCallback = onSaveSuccessCallback;
    }


    public void setUIContext(UIContext uiContext) {
        this.uiContext = uiContext;
        currentSortMode = uiContext.appState().getCurrentSortByMode();
        setupListView();
        setupPlay();
    }

    public void setViewLoader(ViewLoader viewLoader) {
        this.viewLoader = viewLoader;
    }

    public void init() {
        sort(uiContext.appState().getCurrentSortByMode());
    }

    public void setData(List<Track> tracks) {
        this.currentData = tracks != null ? tracks : new ArrayList<>();
        applyFilterAndSort();
    }

    @FXML
    protected void initialize() {
        setupSort();
        setupRefresh();
        setupSearch();
    }

    private void sort(SortByModes mode) {
        AppState appState = uiContext.appState();
        this.currentSortMode = mode;
        if (btnSort != null) {
            btnSort.setText("sort by " + mode.toString().toLowerCase());
        }
        appState.setCurrentSortByMode(mode);
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
        System.out.println("working-list:" + workingList.size());

        uiContext.playerService().setCurrentList(workingList);

    }

    private void setupListView() {
        PlayerService playerService = uiContext.playerService();
        contentList.setCellFactory(lv -> new MediaTrackCell(playerService, viewLoader, onSaveSuccessCallback));
        contentList.setOnMouseClicked(e -> {
            Track selected = contentList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                playerService.setSelectTrack(selected);
                if (e.getClickCount() == 2) {
                    playerService.playSelectedTrack();
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
        PlayerService playerService = uiContext.playerService();
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

}