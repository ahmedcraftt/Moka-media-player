package cli;

import application.sevice.LibraryService;
import application.sevice.MediaService;
import com.ahmed.utils.InputUtils;
import domain.model.Track;
import infrastructure.audio.AudioPlayer;
import domain.audio.RepeatMode;
import domain.library.Library;

import java.nio.file.Path;
import java.util.List;

public class CliApp {

    private final MediaService mediaService;
    private final AudioPlayer player;
    private final LibraryService libraryService;

    public CliApp(MediaService mediaService, AudioPlayer player, LibraryService libraryService) {
        this.mediaService = mediaService;
        this.player = player;
        this.libraryService = libraryService;
    }

    public void start() {

        initializeLibrary();

        loadLibrary();

        player.setRepeatMode(RepeatMode.LOOP_CURRENT_ONE);

        System.out.println("🎵 Moka Player CLI");
        System.out.println("====================");

        while (true) {
            printMenu();

            int choice = InputUtils.readInt("Choose: ");

            switch (choice) {
                case 1 -> showTracks(mediaService.getTracks());
                case 2 -> showTracks(mediaService.getSongs());
                case 3 -> playTrack();
                case 4 -> next();
                case 5 -> previous();
                case 6 -> stop();
                case 7 -> setRepeatMode();
                case 0 -> {
                    System.out.println("👋 Exiting...");
                    return;
                }
                default -> System.out.println("❌ Invalid choice");
            }
        }
    }

    private void setRepeatMode(){
        printModes();
        int option = InputUtils.readInt("Choose:");
        switch (option){
            case 1 -> player.setRepeatMode(RepeatMode.PLAY_ONE);
            case 2 -> player.setRepeatMode(RepeatMode.LOOP_CURRENT_ONE);
            case 3 -> player.setRepeatMode(RepeatMode.STOP_WHEN_QUEUE_END);
            case 4 -> player.setRepeatMode(RepeatMode.LOOP_CURRENT_QUEUE);
        }
    }

    private void printMenu() {
        System.out.println("""
                
                1. Show All Tracks
                2. Show Songs
                3. Play Track
                4. Next
                5. Previous
                6. Stop
                7. mode
                0. Exit
                """);
    }
    private void printModes(){
        int i = 1;
        for(RepeatMode mode : RepeatMode.values()){
            System.out.println((i++) +". "+ mode.toString());
        }
    }

    private void loadLibrary() {
        mediaService.loadActiveLibrary();
        System.out.println("✅ Loaded media");
        showTracks(mediaService.getTracks());
        player.enqueueAll(mediaService.getTracks());
    }

    private void initializeLibrary() {

        if (!libraryService.hasLibraries()||!libraryService.hasActiveLibrary()) {

            System.out.println("⚠ No libraries found.");
            System.out.println("Please create your first library.");

            String path = InputUtils.readString("Enter music folder path: ");
            String name = InputUtils.readString("Library name: ");

            Library library =
                    libraryService.createLibrary(
                            name,
                            Path.of(path)
                    );

            libraryService.setActiveLibrary(library);
            mediaService.loadActiveLibrary();

            return;
        }

        if (!libraryService.hasActiveLibrary()) {
            libraryService.setActiveLibrary(
                    libraryService.getLibraries().getFirst()
            );
        }

        mediaService.loadActiveLibrary();
    }

    private void showTracks(List<Track> tracks) {
        if (tracks.isEmpty()) {
            System.out.println("⚠ No tracks found");
            return;
        }

        for (int i = 0; i < tracks.size(); i++) {
            Track t = tracks.get(i);
            System.out.printf("%d. %s (%ds)%n", i, t.getMetadata().getTitle(), t.getMetadata().getDurationInSeconds());
        }
    }

    private void playTrack() {
        List<Track> tracks = mediaService.getTracks();

        if (tracks.isEmpty()) {
            System.out.println("⚠ No tracks loaded");
            return;
        }

        showTracks(tracks);

        int index = InputUtils.readInt("Select track index: ", 0, tracks.size() - 1);

        Track selected = tracks.get(index);

        player.playFromList(selected,tracks);

        System.out.println("▶ Playing: " + selected.getMetadata().getTitle());
    }

    private void next() {
        player.playNext();
        System.out.println("⏭ Next track "+ player.getCurrentTrack().getMetadata().getTitle());
    }

    private void previous() {
        player.playPrev();
        System.out.println("⏮ Previous track "+ player.getCurrentTrack().getMetadata().getTitle());
    }

    private void stop() {
        player.stop();
        System.out.println("⏹ Stopped");
    }
}