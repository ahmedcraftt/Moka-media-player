package domain.audio;

import domain.model.media.Track;

public interface PlaybackListener {

    void onTrackChanged(Track track);

    void onPlaybackStateChanged(PlaybackState state);

    void onVolumeChanged(int newVolume);

    void onShuffleChanged(boolean shuffle);
}