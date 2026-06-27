package gui.controllers;

import application.service.LibraryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import domain.model.library.Library;
import domain.model.library.LibraryFolder;

import java.io.File;
import java.nio.file.Path;

public class FoldersViewController {

    @FXML
    private TextField tfSearchBar;

    @FXML
    private Button btnAdd;

    @FXML
    private ListView<LibraryFolder> lvFoldersList;

    private LibraryService libraryService;

    private final ObservableList<LibraryFolder> masterList =
            FXCollections.observableArrayList();

    private FilteredList<LibraryFolder> filteredFolders;

    public void setLibraryService(LibraryService libraryService) {
        this.libraryService = libraryService;
        refreshFolders();
    }

    @FXML
    public void initialize() {
        setupListView();
        setupSearch();
        setupButtons();
    }

    private void setupListView() {

        filteredFolders = new FilteredList<>(masterList);

        lvFoldersList.setItems(filteredFolders);

        lvFoldersList.setCellFactory(list -> new ListCell<>() {

            @Override
            protected void updateItem(LibraryFolder folder, boolean empty) {
                super.updateItem(folder, empty);

                if (empty || folder == null) {
                    setText(null);
                    return;
                }

                String recursive = folder.isRecursive()
                        ? "Recursive"
                        : "Flat";

                String enabled = folder.isEnabled()
                        ? "Enabled"
                        : "Disabled";

                setText(
                        folder.getPath()
                                + " | "
                                + recursive
                                + " | "
                                + enabled
                );
            }
        });
    }

    private void setupSearch() {

        tfSearchBar.textProperty().addListener((obs, oldValue, newValue) -> {

            String query = (newValue == null)
                    ? ""
                    : newValue.toLowerCase().trim();

            filteredFolders.setPredicate(folder -> {

                if (query.isEmpty()) {
                    return true;
                }

                return folder.getPath()
                        .toString()
                        .toLowerCase()
                        .contains(query);
            });
        });
    }

    private void setupButtons() {
        btnAdd.setOnAction(event -> addFolder());
    }

    private void addFolder() {

        if (libraryService == null) {
            showError("Library service not initialized.");
            return;
        }

        Library activeLibrary =
                libraryService.getActiveLibrary();

        if (activeLibrary == null) {
            showError("No active library available.");
            return;
        }

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Folder");

        File selected =
                chooser.showDialog(btnAdd.getScene().getWindow());

        if (selected == null) {
            return;
        }

        Path path = selected.toPath();

        boolean alreadyExists =
                activeLibrary.getFolders()
                        .stream()
                        .anyMatch(folder ->
                                folder.getPath().equals(path)
                        );

        if (alreadyExists) {
            showError("Folder already exists in library.");
            return;
        }

        LibraryFolder folder =
                new LibraryFolder(path, true, true);

        activeLibrary.addFolder(folder);

        libraryService.save();

        // 🔥 update UI instantly
        masterList.add(folder);
    }

    private void refreshFolders() {

        if (libraryService == null) {
            return;
        }

        Library activeLibrary =
                libraryService.getActiveLibrary();

        if (activeLibrary == null) {
            return;
        }

        masterList.setAll(activeLibrary.getFolders());
    }

    private void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}