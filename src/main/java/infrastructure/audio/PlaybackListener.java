package infrastructure.audio;

import domain.model.Track;

public interface PlaybackListener {

    void onTrackChanged(Track newTrack);

    void onPlaybackStateChanged(PlaybackState state);
}