package domain.audio;

public interface TrackListener {
    void onFavoriteChanged(boolean favorite);

    void onTitleChanged(String title);
}
