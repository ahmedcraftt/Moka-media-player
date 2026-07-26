package application.service;

import config.AppConfig;
import domain.model.media.Displayable;

import gui.models.SortByModes;
import gui.models.ViewMode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AppState {

    private final ObservableList<Displayable> currentView =
            FXCollections.observableArrayList();

    private ViewMode currentCategoryMode;

    private SortByModes currentSortByMode;

    public AppState(AppConfig appConfig) {
        currentSortByMode = appConfig.getUIConfig().getStartingSortByMode();
    }

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
