package gui.utils;

import javafx.scene.image.Image;

public class FXUtils {

    public static Image convertToImage(byte[] data) {
        if (data == null || data.length == 0) return null;

        return new Image(new java.io.ByteArrayInputStream(data));
    }


}
