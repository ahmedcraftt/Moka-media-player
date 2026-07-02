package application.service;

import domain.model.media.Displayable;

import gui.controllers.ViewMode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AppState {
    private final ObservableList<Displayable> currentView =
            FXCollections.observableArrayList();

    private ViewMode currentCategoryMode;

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
}
