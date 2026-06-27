package application.service;

import domain.model.media.Displayable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AppState {
    private final ObservableList<Displayable> currentView =
            FXCollections.observableArrayList();

    public ObservableList<Displayable> getCurrentView() {
        return currentView;
    }

    public void setCurrentView(ObservableList<Displayable> currentView) {
        this.currentView.setAll(currentView);
    }
}
