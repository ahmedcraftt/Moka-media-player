package gui.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public final class DialogFactory {

    private static void style(Dialog<?> dialog) {
        DialogPane pane = dialog.getDialogPane();
        boolean add = pane.getStylesheets().add(
                Objects.requireNonNull(DialogFactory.class.getResource("/styles/main.css")).toExternalForm()
        );
        pane.getStyleClass().add("app-dialog");
        Stage stage = (Stage) pane.getScene().getWindow();
        stage.getIcons().add(new Image(
                Objects.requireNonNull(DialogFactory.class.getResourceAsStream("/assets/icons/app-icon.png"))
        ));
    }

    public static Alert confirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        setData(alert, title, header, content);
        return alert;
    }

    public static Alert warnings(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        setData(alert, title, header, content);
        return alert;
    }

    public static Alert error(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        setData(alert, title, header, content);
        return alert;
    }

    private static void setData(Alert alert, String title, String header, String content) {
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        style(alert);
    }
}
