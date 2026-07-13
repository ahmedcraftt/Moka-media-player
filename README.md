# MOKA PLAYER ☕🎵

Moka Player is a desktop music player built with JavaFX and VLCJ.  
It focuses on clean architecture, modular design, and media organization.

---

## ⚠ Disclaimer

This project is still under active development and is not feature-complete.  
Some features are experimental, incomplete, or subject to change.

This project is primarily for learning and architecture exploration,  
so refactoring may temporarily break existing features.

---

## Features

- Audio playback using VLCJ (libVLC backend)
- Directory scanning for audio files
- Automatic metadata extraction (title, artist, genre, etc.)
- Support for songs, podcasts, and audiobooks
- Playlist system
- Search and filtering
- Shuffle and repeat modes
- Volume and playback control
- Clean layered architecture

---

## Architecture Overview

The project follows a layered architecture:

### UI Layer
- JavaFX controllers
- Handles user interaction and display only

### Application Layer
- MediaService: manages media loading and organization
- PlayerService: handles playback logic and state

### Infrastructure Layer
- MediaScanner: scans filesystem for audio files
- VLCJAudioEngine: handles audio playback
- JaudiotaggerManager: extracts metadata

### Domain Layer
- Track
- Playlist

---

## How it works

1. User selects a music directory
2. MediaService triggers MediaScanner
3. Scanner finds supported audio files
4. Metadata is extracted
5. Track objects are created
6. UI displays filtered/sorted views
7. PlayerService handles playback

---

## Supported formats

mp3, flac, wav, m4a, ogg, aac, opus, wma, alac, aiff, amr, mid, ra  
(Any format supported by VLC)

---

## Future Improvements

- Persistent playlists
- Waveform visualization
- Equalizer system
- Live library updates
- Improved queue management

---
## How to Clone and Run

### 1. Clone the repository
```bash
git clone https://github.com/your-username/moka-player.git
cd moka-player
```
---

## License

Free use.
