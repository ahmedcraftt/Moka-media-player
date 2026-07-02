package gui.controllers;

import application.service.MediaService;
import domain.model.metadata.Filedata;
import domain.model.metadata.Metadata;
import domain.model.media.MediaType;
import domain.model.media.Track;
import infrastructure.media.MetadataManager;
import infrastructure.storage.MetadataStorage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.Year;

public class TrackDataViewController {

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

    private MetadataManager metadataManager;
    private MetadataStorage storage;
    private MediaService mediaService;


    public void setTrack(Track track) {
        this.track = track;

        if (track == null) {
            return;
        }

        loadTrackData();
    }

    public void setMetadataManager(MetadataManager metadataManager) {
        this.metadataManager = metadataManager;
    }

    public void setStorage(MetadataStorage storage) {
        this.storage = storage;
    }

    public void setMediaService(MediaService mediaService) {
        this.mediaService = mediaService;
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
        tfSamplerate.setText(String.valueOf(metadata.getSamplerate()));
        tfLang.setText(safe(metadata.getLanguage()));
        tfTitle.setText(safe(metadata.getTitle()));
        tfGenre.setText(safe(metadata.getGenre()));
        taDescription.setText(safe(metadata.getDescription()));
        tfArtist.setText(safe(metadata.getArtist()));
        tfAlbumArtist.setText(safe(metadata.getSeriesArtist()));
        tfAlbum.setText(safe(metadata.getSeries()));
        tfAlbumNumber.setText(String.valueOf(metadata.getTrackNumber()));

        if (metadata.getYear() != null)
            tfYear.setText(String.valueOf(metadata.getYear().getValue()));

        if (track.getType() == MediaType.SONG) {
            lblArtist.setText("Artist");
            lblAlbumArtist.setText("Album Artist");
            lblAlbum.setText("Album");
            lblAlbumNumber.setText("Album Number");
        }
        if (track.getType() == MediaType.AUDIOBOOK) {
            lblArtist.setText("Author");
            lblAlbumArtist.setText("Narrator");
            lblAlbum.setText("Series");
            lblAlbumNumber.setText("Chapter Number");
        }
        if (track.getType() == MediaType.PODCAST) {
            lblArtist.setText("Host");
            lblAlbum.setText("Series");
            lblAlbumArtist.setText("Channel");
            lblAlbumNumber.setText("Episode Number");
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
        IO.println("TrackDataViewController line 178:" + track.getType());
        metadata.setTitle(tfTitle.getText().trim());
        metadata.setGenre(tfGenre.getText().trim());
        metadata.setLanguage(tfLang.getText().trim());
        metadata.setDescription(taDescription.getText().trim());
        metadata.setArtist(tfArtist.getText().trim());
        metadata.setSeriesArtist(tfAlbumArtist.getText().trim());
        metadata.setSeries(tfAlbum.getText().trim());

        if (!number.isBlank()) {
            try {
                metadata.setTrackNumber(Integer.parseInt(number));
            } catch (NumberFormatException e) {
                System.out.println("Invalid album number");
            }
        }

        metadataManager.write(track);

        storage.update(metadata);

        mediaService.refreshMetadataCaches();

        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.getScene().getRoot().fireEvent(new RefreshEvent());

        closeWindow();
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        if (btnCancel.getScene() != null && btnCancel.getScene().getWindow() != null) {
            Stage stage = (Stage) btnCancel.getScene().getWindow();
            stage.close();
        }
    }

    private String safe(Object value) {
        return value == null || value.toString().isBlank() ? "unknown" : value.toString();
    }
}