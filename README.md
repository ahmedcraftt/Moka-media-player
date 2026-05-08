MOKA PLAYER ☕🎵
Moka Player is a desktop music player built with JavaFX and VLCJ.
It focuses on clean architecture, modular design, and media organization.

DISCLAIMER
This project is still under active development and is not feature-complete.
Some core features are either incomplete, experimental, or subject to change.
You may encounter bugs, missing functionality, or unpolished behavior.
This project is primarily a learning and architecture-focused project,
and ongoing refactoring may temporarily break or change existing features.

FEATURES
Audio playback using VLCJ (libVLC backend)
Directory scanning for audio files
Automatic metadata extraction (title, artist, genre, etc.)
Support for Songs, Podcasts, and Audiobooks
Playlist system
Search and filtering
Shuffle and repeat modes
Volume and playback control
Clean layered architecture

ARCHITECTURE OVERVIEW
The project follows a layered architecture:
UI LAYER
JavaFX controllers
Handles user interaction and display only
APPLICATION LAYER
MediaService: loads and organizes media
PlayerService: handles playback logic
INFRASTRUCTURE LAYER
MediaScanner: scans filesystem for audio files
VLCJAudioEngine: handles actual audio playback
JaudiotaggerManger: extracts metadata from files

DOMAIN LAYER
Track
Playlist

HOW IT WORKS
User selects a directory
MediaService requests MediaScanner to scan files
Scanner filters supported audio formats
Metadata is extracted for each file
Track objects are created
Tracks are stored in MediaLibrary
UI requests filtered or sorted views from MediaService
PlayerService handles playback through AudioEngine

SUPPORTED AUDIO FORMATS
mp3, flac, wav, m4a, ogg, aac, opus, wma, alac, aiff, amr, mid, ra
(aka all formats vlc can support)

REQUIREMENTS
Java 17 or higher
JavaFX SDK
VLC installed (libvlc required)
Proper native VLC linking (jna / vlcj setup)
CURRENT STATUS
Core playback system is functional
Media scanning is working
UI is connected to services
Architecture is under active refinement

HOW TO CLONE AND USE
Clone the repository
Open your terminal and run:
git clone https://github.com/your-username/moka-player.git⁠�
Then navigate into the project:
cd moka-player
Set up requirements
Make sure you have the following installed:
Java 17 or higher
JavaFX SDK
VLC Media Player (libVLC is required for playback)
Important: VLC files {
libvlc
libvlccore
and the plugins folder 
all must be added to the (project folder)/natives/(yourOS) folder
}.
Configure JavaFX
If you are running manually (without a build tool), you need to add JavaFX VM options:
Example:
--module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml
(Replace /path/to/javafx with your actual JavaFX SDK path)
Run the application
Run the main class:
ui.main.MainApplication
From your IDE (recommended):
Open the project
Set VM options (JavaFX path)
Run MainApplication
First launch
The app will scan a default directory (currently hardcoded)
You may want to change this path in the code:
mediaService.loadDirectory(Path.of("your/music/folder"))
Using the app
Use the sidebar to switch between: Tracks / Songs / Podcasts / Books
Use search to filter results
Select a track and press Play
Use bottom controls for playback
KNOWN LIMITATIONS
Directory path is currently hardcoded
Playlist system is basic
UI updates are not fully reactive yet
Some metadata may not load correctly for all files

FUTURE IMPROVEMENTS
Reactive media library updates (live UI updates)
Persistent playlists (save/load from disk)
Audio waveform visualization
Equalizer system
Better queue management

LICENSE
This project is for educational and personal use.
