package gui.controllers;

import application.service.AppState;
import application.service.MediaService;
import application.service.PlayerService;
import domain.model.media.Track;
import gui.controllers.events.RefreshEvent;
import gui.controllers.listcells.MediaTrackCell;
import gui.main.AppContext;

import gui.model.SortByModes;
import gui.utils.ViewLoader;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.*;


public class MediaListViewController {

    @FXML
    private AnchorPane mainRoot;
    @FXML private ListView<Track> contentList;
    @FXML private TextField searchBar;
    @FXML private MenuButton btnSort;
    @FXML
    private Button btnRefresh, btnListPlay, btnAdd;
    @FXML
    private CheckMenuItem cmiAscending;

    private List<Track> currentData = new ArrayList<>();

    private static Runnable onSaveSuccess;

    private ViewLoader viewLoader;
    private AppContext appContext;

    private SortByModes currentSortMode;

    public void setOnSaveSuccessCallback(Runnable onSaveSuccess) {
        MediaListViewController.onSaveSuccess = onSaveSuccess;
    }


    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
        currentSortMode = appContext.appState().getCurrentSortByMode();
        setupListView();
        setupPlay();
        handleAdd();
    }

    public void setViewLoader(ViewLoader viewLoader) {
        this.viewLoader = viewLoader;
    }

    public void init() {
        sort(appContext.appState().getCurrentSortByMode());
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
        AppState appState = appContext.appState();
        this.currentSortMode = mode;

        if (btnSort != null) {
            btnSort.setText("sort by " + mode.toString().toLowerCase());
        }
        appState.setCurrentSortByMode(mode);
        applyFilterAndSort();
    }

    private void handleAdd() {
        btnAdd.setOnAction(event -> addTrack());
    }

    private void addTrack() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a File");

        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio Files",
                        "*.mp3", "*.flac", "*.wav",
                        "*.m4a", "*.ogg", "*.aac",
                        "*.opus", "*.wma", "*.alac"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showOpenDialog(btnAdd.getScene().getWindow());

        if (file == null) return;

        Track added = appContext.mediaScanner().scan(file);
        MediaService mediaService = appContext.mediaService();
        PlayerService playerService = appContext.playerService();

        if (added != null && !mediaService.getTracks().contains(added)) {
            mediaService.addTrack(added);
        }

        playerService.setSelectTrack(added);
        playerService.playSelectedTrack();

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

        Comparator<Track> hierarchicalComparator = getTrackComparator();

        workingList.sort(hierarchicalComparator);

        contentList.getItems().setAll(workingList);

        appContext.playerService().setCurrentList(workingList);

    }

    private Comparator<Track> getTrackComparator() {
        Comparator<Track> baseComparator = switch (currentSortMode) {
            case TITLE -> Comparator.comparing(t -> safe(t.getTitle()), String.CASE_INSENSITIVE_ORDER);

            case FILE_NAME -> Comparator.comparing(t -> safe(t.getFileName()), String.CASE_INSENSITIVE_ORDER);
            case ARTISTS ->
                    Comparator.comparing(t -> t.getMetadata() != null ? safe(t.getMetadata().getArtist()) : "", String.CASE_INSENSITIVE_ORDER);
            case DURATION ->
                    Comparator.comparingInt(t -> t.getMetadata() != null ? t.getMetadata().getDurationInSeconds() : 0);
            case YEAR -> Comparator.comparingInt(t -> (t.getMetadata() != null && t.getMetadata().getYear() != null)
                    ? t.getMetadata().getYear().getValue() : 0);
            case DATE_ADDED ->
                    Comparator.comparing(Track::getDateAdded, Comparator.nullsLast(Comparator.naturalOrder()));
            case DATE_MODIFIED ->
                    Comparator.comparing(t -> t.getFiledata() != null ? t.getFiledata().getDateModified() : null,
                            Comparator.nullsLast(Comparator.naturalOrder()));
            case DATE_CREATED ->
                    Comparator.comparing(t -> t.getFiledata() != null ? t.getFiledata().getDateCreated() : null,
                            Comparator.nullsLast(Comparator.naturalOrder()));
            default -> throw new IllegalStateException("Unexpected value: " + currentSortMode);
        };

        if (cmiAscending != null && !cmiAscending.isSelected()) baseComparator = baseComparator.reversed();

        return Comparator.comparing((Track t) -> !t.isFavorite())
                .thenComparing(baseComparator);
    }

    private void setupListView() {
        PlayerService playerService = appContext.playerService();
        contentList.setCellFactory(lv -> new MediaTrackCell(playerService, viewLoader, onSaveSuccess));
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
        PlayerService playerService = appContext.playerService();
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