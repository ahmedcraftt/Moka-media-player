package gui.controllers.listcells;

import domain.model.media.Track;
import javafx.scene.control.ListView;

public class PlayListCreationCell extends CheckedTrackCell {
    public PlayListCreationCell() {
        super();
        selectedCheckBox.setOnAction(e -> {
            ListView<Track> lv = getListView();
            if (lv == null) return;

            if (selectedCheckBox.isSelected()) {
                lv.getSelectionModel().select(getIndex());
            } else {
                lv.getSelectionModel().clearSelection(getIndex());
            }
        });
        root.getChildren().add(selectedCheckBox);
    }

    @Override
    protected void updateItem(Track item, boolean empty) {
        if (getListView() != null) {
            selectedCheckBox.setSelected(getListView().getSelectionModel().isSelected(getIndex()));
        }
        super.updateItem(item, empty);
    }
}
