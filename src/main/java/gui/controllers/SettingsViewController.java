package gui.controllers;

import application.service.PlayerService;
import config.PlayerConfig;
import config.SearchConfig;
import config.UIConfig;
import gui.controllers.events.UpdateEvent;
import gui.main.AppContext;
import gui.models.SearchEngine;
import gui.models.TabsLocation;

import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class SettingsViewController {
    @FXML
    private VBox root;
    @FXML
    private Button btnHideTabs, btnSkip, btnVolume, btnVolumeModifier,
            btnLyricsAlignment, btnSearch, btnTabsLocation;
    @FXML
    private ContextMenu cxmHideTabs, cxmSkip, cxmVolume, cxmVolumeModifier,
            cxmLyricsAlignment, cxmSearch, cxmTabsLocation;
    @FXML
    private CheckMenuItem cmiTracks, cmiSongs, cmiBooks, cmiPodcasts, cmiAlbums, cmiArtists, cmiGenres, cmiPlaylist;
    @FXML
    private MenuItem miFive, miTen, miFifteen, miTwenty;
    @FXML
    private MenuItem miFiveVolume, miTenVolume, miFifteenVolume, miTwentyVolume;
    @FXML
    private MenuItem miFifty, miHundred;
    @FXML
    private MenuItem miLeft, miRight, miCenter;
    @FXML
    private MenuItem miGoogle, miDuck, miBing, miBrave;
    @FXML
    private MenuItem miLeftSide, miTopSide;

    private AppContext appContext;

    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
        init();
    }

    private void init() {
        setupHideTabs();
        setupVolumeModifier();
        setupVolume();
        setupLyricsAlignment();
        setupSkip();
        setupSearchEngine();
        setupTabsLocation();
    }

    @FXML
    private void initialize() {
        setupContextMenu();
    }

    private void setupContextMenu() {
        btnHideTabs.setOnAction(event -> cxmHideTabs.show(btnHideTabs, Side.BOTTOM, 0, 0));
        btnSkip.setOnAction(event -> cxmSkip.show(btnSkip, Side.BOTTOM, 0, 0));
        btnVolume.setOnAction(event -> cxmVolume.show(btnVolume, Side.BOTTOM, 0, 0));
        btnVolumeModifier.setOnAction(event -> cxmVolumeModifier.show(btnVolumeModifier, Side.BOTTOM, 0, 0));
        btnLyricsAlignment.setOnAction(event -> cxmLyricsAlignment.show(btnLyricsAlignment, Side.BOTTOM, 0, 0));
        btnSearch.setOnAction(event -> cxmSearch.show(btnSearch, Side.BOTTOM, 0, 0));
        btnTabsLocation.setOnAction(event -> cxmTabsLocation.show(btnTabsLocation, Side.BOTTOM, 0, 0));
    }

    private void setupHideTabs() {
        UIConfig config = appContext.config().getUIConfig();

        cmiTracks.setSelected(config.isTracksBtnVisibility());
        cmiSongs.setSelected(config.isSongsBtnVisibility());
        cmiBooks.setSelected(config.isBooksBtnVisibility());
        cmiPodcasts.setSelected(config.isPodcastsBtnVisibility());
        cmiAlbums.setSelected(config.isAlbumsBtnVisibility());
        cmiArtists.setSelected(config.isArtistsBtnVisibility());
        cmiGenres.setSelected(config.isGenresBtnVisibility());
        cmiPlaylist.setSelected(config.isPlaylistsBtnVisibility());

        cmiTracks.setOnAction(event -> {
            config.setTracksBtnVisibility(cmiTracks.isSelected());
            btnHideTabs.fireEvent(new UpdateEvent(UpdateEvent.UPDATE));
        });
        cmiSongs.setOnAction(event -> {
            config.setSongsBtnVisibility(cmiSongs.isSelected());
            root.fireEvent(new UpdateEvent(UpdateEvent.UPDATE));
        });
        cmiBooks.setOnAction(event -> {
            config.setBooksBtnVisibility(cmiBooks.isSelected());
            root.fireEvent(new UpdateEvent(UpdateEvent.UPDATE));
        });
        cmiPodcasts.setOnAction(event -> {
            config.setPodcastsBtnVisibility(cmiPodcasts.isSelected());
            root.fireEvent(new UpdateEvent(UpdateEvent.UPDATE));
        });
        cmiAlbums.setOnAction(event -> {
            config.setAlbumsBtnVisibility(cmiAlbums.isSelected());
            root.fireEvent(new UpdateEvent(UpdateEvent.UPDATE));
        });
        cmiArtists.setOnAction(event -> {
            config.setArtistsBtnVisibility(cmiArtists.isSelected());
            root.fireEvent(new UpdateEvent(UpdateEvent.UPDATE));
        });
        cmiGenres.setOnAction(event -> {
            config.setGenresBtnVisibility(cmiGenres.isSelected());
            root.fireEvent(new UpdateEvent(UpdateEvent.UPDATE));
        });
        cmiPlaylist.setOnAction(event -> {
            config.setPlaylistsBtnVisibility(cmiPlaylist.isSelected());
            root.fireEvent(new UpdateEvent(UpdateEvent.UPDATE));
        });
    }

    private void setupVolumeModifier() {
        PlayerConfig config = appContext.config().getPlayerConfig();
        PlayerService service = appContext.playerService();
        miFiveVolume.setOnAction(event -> {
            service.setVolume(5);
            config.setPreferredVolumeModifier(5);
            root.fireEvent(new UpdateEvent(UpdateEvent.UPDATE));
        });
        miTenVolume.setOnAction(event -> {
            service.setVolume(10);
            config.setPreferredVolumeModifier(10);
            root.fireEvent(new UpdateEvent(UpdateEvent.UPDATE));
        });
        miFifteenVolume.setOnAction(event -> {
            service.setVolume(15);
            config.setPreferredVolumeModifier(15);
            root.fireEvent(new UpdateEvent(UpdateEvent.UPDATE));
        });
        miTwentyVolume.setOnAction(event -> {
            service.setVolume(20);
            config.setPreferredVolumeModifier(20);
            root.fireEvent(new UpdateEvent(UpdateEvent.UPDATE));
        });

    }

    private void setupVolume() {
        PlayerConfig config = appContext.config().getPlayerConfig();
        miFifty.setOnAction(event -> {
            config.setPreferredVolumeLevel(50);
        });
        miHundred.setOnAction(event -> {
            config.setPreferredVolumeLevel(100);
        });
    }

    private void setupLyricsAlignment() {
        UIConfig config = appContext.config().getUIConfig();
        miLeft.setOnAction(event -> {
            config.setLyricsTextAlignment(TextAlignment.LEFT);
        });

        miRight.setOnAction(event -> {
            config.setLyricsTextAlignment(TextAlignment.RIGHT);
        });

        miCenter.setOnAction(event -> {
            config.setLyricsTextAlignment(TextAlignment.CENTER);
        });

    }

    private void setupSkip() {
        PlayerConfig config = appContext.config().getPlayerConfig();

        miFive.setOnAction(event -> {
            config.setPreferredSkipSeconds(5);
            root.fireEvent(new UpdateEvent(UpdateEvent.UPDATE));
        });
        miTen.setOnAction(event -> {
            config.setPreferredSkipSeconds(10);
            root.fireEvent(new UpdateEvent(UpdateEvent.UPDATE));
        });
        miFifteen.setOnAction(event -> {
            config.setPreferredSkipSeconds(15);
            root.fireEvent(new UpdateEvent(UpdateEvent.UPDATE));
        });
        miTwenty.setOnAction(event -> {
            config.setPreferredSkipSeconds(20);
            root.fireEvent(new UpdateEvent(UpdateEvent.UPDATE));
        });
    }

    private void setupSearchEngine() {
        SearchConfig config = appContext.config().getSearchConfig();
        miGoogle.setOnAction(event -> {
            config.setPreferredSearchEngine(SearchEngine.GOOGLE);
        });
        miDuck.setOnAction(event -> {
            config.setPreferredSearchEngine(SearchEngine.DUCK_DUCK_GO);
        });
        miBing.setOnAction(event -> {
            config.setPreferredSearchEngine(SearchEngine.BING);
        });
        miBrave.setOnAction(event -> {
            config.setPreferredSearchEngine(SearchEngine.BRAVE);
        });
    }

    private void setupTabsLocation() {
        UIConfig config = appContext.config().getUIConfig();
        miLeftSide.setOnAction(event -> {
            config.setPreferredTabsLocation(TabsLocation.LEFT);
            root.fireEvent(new UpdateEvent(UpdateEvent.UPDATE));
        });
        miTopSide.setOnAction(event -> {
            config.setPreferredTabsLocation(TabsLocation.TOP);
            root.fireEvent(new UpdateEvent(UpdateEvent.UPDATE));
        });
    }
}
