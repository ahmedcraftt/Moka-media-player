package gui.controllers;

import domain.model.Filedata;
import domain.model.MediaType;
import domain.model.Metadata;
import domain.model.Track;

import infrastructure.media.JaudiotaggerManager;
import infrastructure.media.MetadataManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.Year;

public class TrackDataEditViewController {

    // Read-only file info and metadata

    @FXML
    private TextField tfFilePath;
    @FXML
    private TextField tfFilename;
    @FXML
    private TextField tfDateCreated;
    @FXML
    private TextField tfDateModified;
    @FXML
    private TextField tfTimesPlayed;
    @FXML
    private TextField tfFileType;
    @FXML
    private TextField tfFileSize;
    @FXML
    private TextField tfLastAccessed;
    @FXML
    private TextField tfBitrate;
    @FXML
    private TextField tfSamplerate;

    // Editable metadata

    @FXML
    private TextField tfType;
    @FXML
    private TextField tfTitle;
    @FXML
    private TextField tfGenre;
    @FXML
    private TextField tfYear;
    @FXML
    private TextField tfArtist;
    @FXML
    private TextField tfAlbumArtist;
    @FXML
    private TextField tfAlbum;
    @FXML
    private TextField tfAlbumNumber;
    @FXML
    private TextField tfLang;

    @FXML
    private TextArea taDescription;

    @FXML
    private Label lblArtist;
    @FXML
    private Label lblAlbumNumber;
    @FXML
    private Label lblAlbum;
    @FXML
    private Label lblAlbumArtist;


    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;

    private Track track;

    public void setTrack(Track track) {
        this.track = track;

        if (track == null) {
            return;
        }

        loadTrackData();
    }

    private void loadTrackData() {

        // File data

        Filedata filedata = track.getFiledata();

        tfFilename.setText(track.getFiledata().getFileName());
        tfTimesPlayed.setText(String.valueOf(track.getTimesPlayed()));
        tfFilePath.setText(filedata.getFilePathString());
        tfDateCreated.setText(filedata.getDateCreatedString());
        tfDateModified.setText(filedata.getDateModifiedString());
        tfFileType.setText(filedata.getFileType());
        tfFileSize.setText(String.valueOf(filedata.getFileSize()));
        tfLastAccessed.setText(filedata.getLastAccessedString());

        // Metadata

        Metadata metadata = track.getMetadata();

        tfType.setText(safe(track.getType().getTitle()));
        tfBitrate.setText(String.valueOf(metadata.getBitrate()));
        tfSamplerate.setText(String.valueOf(metadata.getSampleRate()));
        tfLang.setText(safe(metadata.getLanguage()));
        tfTitle.setText(safe(metadata.getTitle()));
        tfGenre.setText(safe(metadata.getGenre()));
        taDescription.setText(safe(metadata.getDescription()));
        if (track.getType() == MediaType.SONG) {
            lblArtist.setText("Artist");
            tfArtist.setText(safe(metadata.getArtist()));
            lblAlbumArtist.setText("Album Artist");
            tfAlbumArtist.setText(safe(metadata.getAlbumArtist()));
            lblAlbum.setText("Album");
            tfAlbum.setText(safe(metadata.getAlbum()));
            lblAlbumNumber.setText("Album Number");
            tfAlbumNumber.setText(String.valueOf(metadata.getAlbumNumber()));
        }
        if (track.getType() == MediaType.AUDIOBOOK) {
            lblArtist.setText("Author");
            tfArtist.setText(safe(metadata.getAuthor()));
            lblAlbumArtist.setText("Narrator");
            tfAlbumArtist.setText(safe(metadata.getNarrator()));
            lblAlbum.setText("Series");
            tfAlbum.setText(safe(metadata.getSeries()));
            lblAlbumNumber.setText("Chapter Number");
            tfAlbumNumber.setText(safe(metadata.getChapterNumber()));
        }
        if (track.getType() == MediaType.PODCAST) {
            lblArtist.setText("Host");
            tfArtist.setText(safe(metadata.getHost()));
            lblAlbum.setText("Series");
            tfAlbum.setText(safe(metadata.getSeries()));
            lblAlbumArtist.setText("Channel");
            tfAlbumArtist.setText(safe(metadata.getChannel()));
            lblAlbumNumber.setText("Episode Number");
            tfAlbumNumber.setText(safe(metadata.getEpisodeNumber()));
        }
        if (metadata.getYear() != null) {
            tfYear.setText(String.valueOf(metadata.getYear().getValue()));
        }


    }

    @FXML
    public void handleSave(ActionEvent event) {

        if (track == null) {
            return;
        }

        Metadata metadata = track.getMetadata();

        String yearText = tfYear.getText().trim();

        if (!yearText.isBlank()) {
            try {
                metadata.setYear(Year.of(Integer.parseInt(yearText)));
            } catch (NumberFormatException e) {
                System.out.println("Invalid year value");
            }
        }
        String number = tfAlbumNumber.getText().trim();

        track.setType(MediaType.StringToMediaType(tfType.getText()));
        metadata.setTitle(tfTitle.getText().trim());
        metadata.setGenre(tfGenre.getText().trim());
        metadata.setLanguage(tfLang.getText().trim());
        metadata.setDescription(taDescription.getText().trim());

        if (track.getType() == MediaType.SONG) {
            metadata.setArtist(tfArtist.getText().trim());
            metadata.setAlbumArtist(tfAlbumArtist.getText().trim());
            metadata.setAlbum(tfAlbum.getText().trim());

            if (!number.isBlank()) {
                try {
                    metadata.setAlbumNumber(Integer.parseInt(number));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid album number");
                }
            }

        } else if (track.getType() == MediaType.AUDIOBOOK) {
            metadata.setAuthor(tfArtist.getText().trim());
            metadata.setNarrator(tfAlbumArtist.getText().trim());
            metadata.setSeries(tfAlbum.getText().trim());

            if (!number.isBlank()) {
                try {
                    metadata.setChapterNumber(Integer.parseInt(number));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid album number");
                }
            }
        } else if (track.getType() == MediaType.PODCAST) {
            metadata.setHost(tfArtist.getText().trim());
            metadata.setChannel(tfAlbumArtist.getText().trim());
            metadata.setSeries(tfAlbum.getText().trim());

            if (!number.isBlank()) {
                try {
                    metadata.setEpisodeNumber(Integer.parseInt(number));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid album number");
                }
            }
        }

        MetadataManager metadataManager = new JaudiotaggerManager();
        metadataManager.write(track);

        closeWindow();
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private String safe(Object value) {
        return value == null || value.toString().isBlank() ? "unknown" : value.toString();
    }
}