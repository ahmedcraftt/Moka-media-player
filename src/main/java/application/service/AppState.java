package application.service;

import domain.model.media.Displayable;

import gui.controllers.SortByModes;
import gui.controllers.ViewMode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AppState {
    private static final SortByModes DEFAULT_STARTING_SORT_BY_MODE = SortByModes.TITLE;

    private final ObservableList<Displayable> currentView =
            FXCollections.observableArrayList();

    private ViewMode currentCategoryMode;

    private SortByModes currentSortByMode = DEFAULT_STARTING_SORT_BY_MODE;

    public ObservableList<Displayable> getCurrentView() {
        return currentView;
    }

    public void setCurrentView(ObservableList<Displayable> currentView) {
        this.currentView.setAll(currentView);
    }

    public ViewMode getCurrentCategoryMode() {
        return currentCategoryMode;
    }

    public void setCurrentCategoryMode(ViewMode currentCategoryMode) {
        this.currentCategoryMode = currentCategoryMode;
    }

    public void clearCurrentView() {
        this.currentView.clear();
    }

    public SortByModes getCurrentSortByMode() {
        return currentSortByMode;
    }

    public void setCurrentSortByMode(SortByModes currentSortByMode) {
        this.currentSortByMode = currentSortByMode;
    }
}
