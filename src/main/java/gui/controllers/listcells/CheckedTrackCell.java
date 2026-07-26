package gui.controllers.listcells;

import javafx.scene.control.CheckBox;

public class CheckedTrackCell extends TrackCell {
    protected final CheckBox selectedCheckBox = new CheckBox();

    public CheckedTrackCell() {
        super();
        lblTitle.setMaxWidth(400);
        lblArtist.setMaxWidth(400);
    }

    public CheckBox getSelectedCheckBox() {
        return selectedCheckBox;
    }


}
