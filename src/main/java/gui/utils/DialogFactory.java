package gui.utils;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public final class DialogFactory {

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

    public static Alert warnings(String title, String header, Node content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        setData(alert, title, header, content);
        return alert;
    }

    public static Alert error(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        setData(alert, title, header, content);
        return alert;
    }

    public static Alert error(String title, String header, Node content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        setData(alert, title, header, content);
        return alert;
    }

    public static TextInputDialog textInputDialog(String title, String header) {
        TextInputDialog dialog = new TextInputDialog();
        setData(dialog, title, header);
        return dialog;
    }

    private static void setData(Dialog<?> dialog, String title, String header) {
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        style(dialog);
    }

    private static void setData(Dialog<?> dialog, String title, String header, String content) {
        setData(dialog, title, header);
        dialog.setContentText(content);
    }

    private static void setData(Dialog<?> dialog, String title, String header, Node content) {
        setData(dialog, title, header);
        dialog.getDialogPane().setContent(content);
    }

    private static void style(Dialog<?> dialog) {
        DialogPane pane = dialog.getDialogPane();
        pane.setMinSize(300, 300);
        boolean add = pane.getStylesheets().add(
                Objects.requireNonNull(DialogFactory.class.getResource("/styles/main.css")).toExternalForm()
        );
        pane.getStyleClass().add("app-dialog");
        Stage stage = (Stage) pane.getScene().getWindow();
        stage.getIcons().add(new Image(
                Objects.requireNonNull(DialogFactory.class.getResourceAsStream("/assets/icons/app-icon.png"))
        ));
    }
}
