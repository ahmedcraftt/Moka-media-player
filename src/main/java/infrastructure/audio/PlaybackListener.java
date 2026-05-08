package infrastructure.audio;

import entities.Track;

public interface PlaybackListener {

    void onTrackChanged(Track newTrack);

    void onPlaybackStateChanged(PlaybackState state);
}