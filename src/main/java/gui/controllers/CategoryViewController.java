package gui.controllers;

import application.service.AppState;
import application.service.PlayerService;
import domain.model.media.Displayable;
import domain.model.media.Playlist;
import domain.model.media.Track;
import gui.utils.TimeFormater;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
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

    private PlayerService playerService;
    private AppState appState;

    private SortByModes currentSortMode = SortByModes.ALPHABETICAL;
    private List<Playlist> rawData;

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public void setAppState(AppState appState) {
        this.appState = appState;
        if (appState.getCurrentCategoryMode() == ViewMode.PLAYLIST) {
            appState.clearCurrentView();
        }
    }

    public void setData(List<Playlist> categoryPlaylists, ViewMode mode) {
        this.rawData = categoryPlaylists;
        if (categoryPlaylists != null) {
            this.categories.setAll(categoryPlaylists);
        }


        if (this.appState != null) {
            ViewMode previousMode = this.appState.getCurrentCategoryMode();

            if (previousMode != null && previousMode != mode || previousMode == ViewMode.PLAYLIST) {
                if (txtSearch != null) {
                    txtSearch.clear();
                }
                this.appState.setCurrentView(FXCollections.emptyObservableList());
                if (contentList != null) {
                    contentList.getSelectionModel().clearSelection();
                }
            }

            this.appState.setCurrentCategoryMode(mode);

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
        if (contentList == null || appState == null) return;

        if (appState.getCurrentView() == null || appState.getCurrentView().isEmpty()) {
            contentList.setItems(filteredCategories);
        } else {
            contentList.setItems(appState.getCurrentView());
        }
    }

    private void setUpListView() {
        if (contentList != null) {
            contentList.setCellFactory(lv -> new CategoryListCell());
            contentList.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> {
                        if (newVal != null) {
                            handleItemClick(newVal);
                        }
                    }
            );
        }
    }

    private void setUpListViewItems() {
        if (contentList == null || appState == null) return;

        Platform.runLater(() -> {
            if (appState.getCurrentView() == null || appState.getCurrentView().isEmpty()) {
                contentList.setItems(filteredCategories);
            } else {
                contentList.setItems(appState.getCurrentView());
            }
        });
    }

    private void handleItemClick(Displayable item) {
        if (item instanceof Playlist p) {
            openCategoryPlaylist(p);
        } else if (item instanceof Track t) {
            playTrack(t);
        }
    }

    private void playTrack(Track t) {
        if (playerService != null) {
            playerService.setSelectTrack(t);
            playerService.playSelectedTrack();
        }
    }

    private void openCategoryPlaylist(Playlist p) {
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

    private static class CategoryListCell extends ListCell<Displayable> {
        private final ImageView artworkView = new ImageView();
        private final Label titleLabel = new Label();
        private final Label infoLabel = new Label();
        private final HBox root = new HBox(15);

        private static Image defaultArtwork;

        public CategoryListCell() {
            if (defaultArtwork == null) {
                try {
                    defaultArtwork = new Image(
                            Objects.requireNonNull(CategoryListCell.class.getResourceAsStream("/assets/images/unknown.jpg")),
                            50, 50, true, true
                    );
                } catch (Exception e) {
                    System.err.println("Fallback category cell asset missing.");
                }
            }

            artworkView.setFitWidth(50);
            artworkView.setFitHeight(50);
            artworkView.setPreserveRatio(true);

            VBox textBox = new VBox(4, titleLabel, infoLabel);
            root.getChildren().addAll(artworkView, textBox);
            root.setAlignment(Pos.CENTER_LEFT);
        }

        @Override
        protected void updateItem(Displayable item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            String artworkPath = item.getArtworkPath();

            if (item instanceof Playlist playlist) {
                String displayTitle = playlist.getTitle() != null ? playlist.getTitle() : "Unknown Category";
                titleLabel.setText(displayTitle);

                int totalTracks = playlist.size();
                infoLabel.setText(totalTracks + " " + (totalTracks == 1 ? "track" : "tracks"));

            } else if (item instanceof Track track) {
                var metadata = track.getMetadata();
                long duration = (metadata != null) ? metadata.getDurationInSeconds() : 0;

                String displayTitle = track.getTitle() != null ? track.getTitle() : "Unknown Track";
                titleLabel.setText(displayTitle + " " + TimeFormater.formatTime(duration));

                infoLabel.setText((metadata != null && metadata.getArtist() != null)
                        ? metadata.getArtist()
                        : "Unknown Artist");
            }

            if (artworkPath != null && !artworkPath.isBlank()) {
                File file = new File(artworkPath);
                if (file.exists()) {
                    artworkView.setImage(new Image(file.toURI().toString(), 50, 50, true, true, true));
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