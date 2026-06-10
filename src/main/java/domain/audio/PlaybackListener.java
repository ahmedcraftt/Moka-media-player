package domain.audio;

import domain.model.Track;

public interface PlaybackListener {

    void onTrackChanged(Track track);

    void onPlaybackStateChanged(PlaybackState state);
}