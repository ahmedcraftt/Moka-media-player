package gui.utils;

import application.service.AppState;
import application.service.LibraryService;
import application.service.MediaService;
import application.service.PlayerService;
import domain.model.library.MediaLibrary;
import infrastructure.audio.AudioPlayer;
import infrastructure.media.MetadataManager;
import infrastructure.scanner.MediaScanner;
import infrastructure.storage.ArtworkStorage;
import infrastructure.storage.MetadataStorage;
import infrastructure.storage.TrackStorage;

public record UIContext(MediaService mediaService, AudioPlayer player, LibraryService libraryService,
                        PlayerService playerService, MetadataManager metadataManager, MediaLibrary mediaLibrary,
                        AppState appState, ArtworkStorage artStorage, MetadataStorage metadataStorage,
                        TrackStorage trackStorage, MediaScanner scanner) {

}
