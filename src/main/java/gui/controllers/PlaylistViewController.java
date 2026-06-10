package gui.controllers;

import application.dto.PlaylistDTO;
import domain.library.MediaLibrary;
import domain.model.Playlist;
import infrastructure.factory.PlaylistFactory;
import infrastructure.mapper.PlaylistMapper;
import infrastructure.storage.PlaylistStorage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.stream.Collectors;

public class PlaylistViewController {

    @FXML
    private ListView<Playlist> listView;
    @FXML
    private TextField tfSearchBar;

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
    private final ObservableList<Playlist> filteredPlaylists = FXCollections.observableArrayList();

    private MediaLibrary mediaLibrary;

    public void setMediaLibrary(MediaLibrary mediaLibrary) {
        this.mediaLibrary = mediaLibrary;
    }

    @FXML
    public void initialize() {

        listView.setItems(filteredPlaylists);
        listView.setCellFactory(lv -> new MyListCell());

        loadPlaylists();
        setupSearch();
        setupButtons();
    }

    private void loadPlaylists() {
        try {
            List<PlaylistDTO> dtos = PlaylistStorage.load();

            playlists.clear();

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
        tfSearchBar.textProperty().addListener((obs, oldVal, newVal) -> {
            applyFilter(newVal);
        });
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

        btnFavorites.setOnAction(e ->
                filteredPlaylists.setAll(
                        playlists.stream()
                                .filter(Playlist::isFavorite)
                                .toList()
                )
        );

        btnRecentlyAdded.setOnAction(e -> {
            filteredPlaylists.setAll(playlists);
        });

        btnMostPlayed.setOnAction(e -> {
            filteredPlaylists.setAll(playlists);
        });

        btnRecentlyPlayed.setOnAction(e -> {
            filteredPlaylists.setAll(playlists);
        });

        btnAdd.setOnAction(e -> createPlaylist());
    }

    private void createPlaylist() {

        Playlist playlist = PlaylistFactory.create("New Playlist");

        //TODO load the creat-listview
        playlists.add(playlist);
        filteredPlaylists.add(playlist);

        try {
            PlaylistStorage.save(playlist);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to save playlist");
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private static class MyListCell extends ListCell<Playlist> {

        private final Label title = new Label();

        @Override
        protected void updateItem(Playlist playlist, boolean empty) {
            super.updateItem(playlist, empty);

            if (empty || playlist == null) {
                setGraphic(null);
                return;
            }

            title.setText(playlist.getTitle());
            setGraphic(title);
        }
    }
}