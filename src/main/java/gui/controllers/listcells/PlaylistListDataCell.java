package gui.controllers.listcells;

import domain.model.media.Track;

import java.util.Set;

public class PlaylistListDataCell extends CheckedTrackCell {
    private final Set<Track> chosenTracksContext;

    public PlaylistListDataCell(Set<Track> chosenTracksContext) {
        this.chosenTracksContext = chosenTracksContext;
        super();
        selectedCheckBox.setOnAction(e -> {
            Track currentItem = getItem();
            if (currentItem == null) return;

            if (selectedCheckBox.isSelected()) {
                chosenTracksContext.add(currentItem);
            } else {
                chosenTracksContext.remove(currentItem);
            }
        });

        root.getChildren().addAll(selectedCheckBox);
    }

    @Override
    protected void updateItem(Track item, boolean empty) {
        selectedCheckBox.setSelected(chosenTracksContext.contains(item));
        super.updateItem(item, empty);
    }
}
