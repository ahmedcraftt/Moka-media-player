package gui.controllers;

import application.dto.PlaylistDTO;
import application.service.AppState;
import application.service.PlayerService;
import domain.model.library.MediaLibrary;
import domain.model.media.Displayable;
import domain.model.media.Playlist;
import domain.model.media.Track;
import gui.utils.ImageConverter;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PlaylistViewController {

    @FXML
    private ListView<Displayable> listView;

    @FXML
    private TextField tfSearchBar;

    @FXML
    private Button btnBack;
    @FXML
    private MenuButton btnSort;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnFavorites;
    @FXML
    private Button btnRecentlyAdded;
    @FXML
    private Button btnMostPlayed;
    @FXML
    private Button btnRecentlyPlayed;

    private final ObservableList<Playlist> playlists = FXCollections.observableArrayList();
    private final ObservableList<Displayable> filteredPlaylists = FXCollections.observableArrayList();
    private final ObservableList<Displayable> previousData = FXCollections.observableArrayList();

    private MediaLibrary mediaLibrary;
    private PlayerService playerService;
    private AppState appState;

    private static List<Track> tracksList;
    private static Runnable onSaveSuccess;

    public void setMediaLibrary(MediaLibrary mediaLibrary) {
        this.mediaLibrary = mediaLibrary;
        loadPlaylists();
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public void setAppState(AppState appState) {
        this.appState = appState;
        setUpListViewItems();
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
    }

    private void setUpListView() {
        listView.setCellFactory(lv -> new MyListCell());
        listView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        handleItemClick(newVal);
                    }
                }
        );

    }

    private void setUpListViewItems() {
        if (appState.getCurrentView().isEmpty()) {
            listView.setItems(filteredPlaylists);
        } else listView.setItems(appState.getCurrentView());
    }

    private void handleItemClick(Displayable item) {
        if (item instanceof Playlist p) {
            openPlaylist(p);
        } else if (item instanceof Track t) {
            playTrack(t);
        }
    }

    private void playTrack(Track t) {
        playerService.setSelectTrack(t);
        playerService.playSelectedTrack();
    }

    private void openPlaylist(Playlist p) {
        openList(p.getTracks());
    }

    private void openList(List<Track> list) {
        ObservableList<Displayable> tracks = FXCollections.observableArrayList();
        tracks.setAll(list);
        playerService.setCurrentList(list);
        Platform.runLater(() -> {
            listView.getSelectionModel().clearSelection();
            listView.setItems(tracks);
            appState.setCurrentView(tracks);
        });
    }



    private void loadPlaylists() {
        try {
            playlists.clear();

            List<PlaylistDTO> dtos = PlaylistStorage.load();

            for (PlaylistDTO dto : dtos) {
                playlists.add(PlaylistMapper.fromDTO(dto, mediaLibrary));
            }

            filteredPlaylists.setAll(playlists);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load playlists");
        }
    }

    private void setupSearch() {
        tfSearchBar.textProperty().addListener((
                obs,
                oldVal,
                newVal
        ) -> applyFilter(newVal));
    }

    private void applyFilter(String query) {
        if (query == null || query.isBlank()) {
            filteredPlaylists.setAll(playlists);
            return;
        }

        String lower = query.toLowerCase();

        filteredPlaylists.setAll(
                playlists.stream()
                        .filter(p -> p.getTitle().toLowerCase().contains(lower))
                        .collect(Collectors.toList())
        );
    }

    private void setupButtons() {

        btnFavorites.setOnAction(e -> setupButton(FilterMode.FAVORITE));

        btnRecentlyAdded.setOnAction(e -> setupButton(FilterMode.RECENTLY_ADDED));

        btnMostPlayed.setOnAction(e -> setupButton(FilterMode.MOST_PLAYED));

        btnRecentlyPlayed.setVisible(false);

        btnAdd.setOnAction(e -> createPlaylist());

        btnBack.setOnAction(event -> {
            listView.setItems(filteredPlaylists);
            appState.setCurrentView(listView.getItems());
        });
    }

    private void setupButton(FilterMode mode) {
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

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/playlist-creation-view.fxml")
            );

            Parent root = loader.load();

            PlaylistCreationViewController controller =
                    loader.getController();

            controller.setTracks(tracksList);

            Image icon = new Image(
                    Objects.requireNonNull(
                            getClass().getResourceAsStream("/assets/icons/app-icon.png")
                    )
            );

            Stage stage = new Stage();
            stage.setTitle("Create playlist");
            stage.setScene(new Scene(root));
            stage.getIcons().add(icon);

            stage.showAndWait();

            Playlist playlist = controller.getCreatedPlaylist();

            if (playlist != null) {

                playlists.add(playlist);
                filteredPlaylists.add(playlist);

                PlaylistStorage.save(playlist);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to create playlist");
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private static void loadPlaylistDataView(Playlist playlist) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                PlaylistViewController.class.getResource("/views/playlist-data-view.fxml")
        );

        Parent root = loader.load();
        PlaylistDataViewController controller = loader.getController();
        controller.setPlaylist(playlist);
        controller.setTracks(tracksList);
        controller.setOnSaveSuccess(onSaveSuccess);

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
    }//loadPlaylistDataView(Playlist playlist)

    private static class MyListCell extends ListCell<Displayable> {

        private final Label title = new Label();
        private final Label info = new Label();
        private final ImageView artworkView = new ImageView();
        private final Button btnInfo = new Button("⋮");

        private final HBox root = new HBox(10);

        public MyListCell() {

            artworkView.setFitWidth(40);
            artworkView.setFitHeight(40);
            artworkView.setPreserveRatio(true);

            VBox textBox = new VBox(5);
            textBox.getChildren().add(title);
            textBox.getChildren().add(info);

            root.getChildren().addAll(artworkView, textBox, btnInfo);
        }

        @Override
        protected void updateItem(Displayable item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
                return;
            }

            Image image = ImageConverter.convertToImage(
                    item.getArtwork()
            );

            artworkView.setImage(image);

            if (item instanceof Track track) {
                title.setText(track.getTitle() + " " + TimeFormater.formatTime(track.getMetadata().getDurationInSeconds()));
                info.setText(track.getMediaMetadata().getArtist());

                btnInfo.setOnAction(event -> {
                    try {
                        MediaListViewController.loadDataView(track);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            if (item instanceof Playlist playlist) {
                title.setText(playlist.getTitle() + " " + TimeFormater.formatTime(playlist.getTotalDurationSeconds()));
                info.setText(playlist.getTracks().getFirst().getTitle());

                btnInfo.setOnAction(event -> {
                    try {
                        loadPlaylistDataView(playlist);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            setGraphic(root);
        }
    }
}