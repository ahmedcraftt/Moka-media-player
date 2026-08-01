package gui.utils;

import platform.OS;
import platform.OSDetector;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public final class DialogLauncher {
    private static final Logger logger = LoggerFactory.getLogger(DialogLauncher.class);

    private DialogLauncher() {
    }

    public static void showFFProbeMissingDialog() {
        VBox box = new VBox(10);
        box.getStylesheets().add("styles/main.css");

        Label text = new Label(
                "Moka requires FFprobe to read media information.\n" +
                        "Download FFmpeg from:"
        );

        Hyperlink link = new Hyperlink("https://ffmpeg.org/download.html");

        link.setOnAction(e -> {
            try {
                if (OSDetector.getOS() == OS.LINUX) {
                    new ProcessBuilder("xdg-open", link.getText()).start();
                } else {
                    Desktop.getDesktop().browse(
                            URI.create(link.getText())
                    );
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        box.getChildren().addAll(text, link);

        Alert alert = DialogFactory.warnings(
                "FFprobe is missing",
                "Unable to find FFprobe",
                box
        );

        alert.showAndWait();
    }

    public static void showVlcMissingDialog(Throwable cause) {
        OS os = OSDetector.getOS();

        Runnable showDialogRunnable = () -> {
            try {

                VBox content = new VBox(10);

                TextArea instructions = new TextArea();
                instructions.setEditable(false);
                instructions.setWrapText(true);
                instructions.setPrefRowCount(15);

                StringBuilder text = new StringBuilder();

                text.append("VLC Media Player native libraries could not be found or loaded.\n\n");
                text.append("Install VLC for your operating system:\n\n");

                if (os.equals(OS.WINDOWS)) {
                    text.append("Windows:\n");
                    text.append("winget install -e --id VideoLAN.VLC\n\n");
                } else if (os.equals(OS.MAC)) {
                    text.append("macOS:\n");
                    text.append("brew install --cask vlc\n\n");
                } else if (os.equals(OS.LINUX)) {
                    text.append("Ubuntu/Debian:\n");
                    text.append("sudo apt install vlc libvlc-dev libvlccore-dev\n\n");

                    text.append("Fedora:\n");
                    text.append("sudo dnf install vlc vlc-devel\n\n");

                    text.append("Arch:\n");
                    text.append("sudo pacman -S vlc\n\n");
                }

                instructions.setText(text.toString());

                Hyperlink vlcLink = new Hyperlink(
                        "https://www.videolan.org/"
                );

                vlcLink.setOnAction(e -> {
                    try {
                        if (OSDetector.getOS() == OS.LINUX) {
                            new ProcessBuilder("xdg-open", vlcLink.getText()).start();
                        } else {
                            Desktop.getDesktop().browse(
                                    URI.create("https://www.videolan.org/")
                            );
                        }
                    } catch (IOException ex) {
                        logger.error("Failed opening VLC website", ex);
                    }
                });

                Label errorLabel = new Label("Native error:");

                TextArea errorBox = new TextArea(
                        cause.getMessage() != null
                                ? cause.getMessage()
                                : cause.toString()
                );

                errorBox.setEditable(false);
                errorBox.setWrapText(true);
                errorBox.setPrefRowCount(5);

                content.getChildren().addAll(
                        instructions,
                        new Label("Download VLC:"),
                        vlcLink,
                        errorLabel,
                        errorBox
                );

                Alert alert = DialogFactory.error(
                        "VLC Dependency Missing",
                        "Unable to Load VLC Native Engine",
                        content
                );

                alert.showAndWait();

            } catch (Throwable dialogException) {
                logger.error("Could not display JavaFX error dialog", dialogException);
            }
        };

        if (Platform.isFxApplicationThread()) {
            showDialogRunnable.run();
        } else {
            Platform.runLater(showDialogRunnable);
        }
    }
}
