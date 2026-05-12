package infrastructure.factory;

import domain.model.Playlist;
import domain.model.Track;

import java.util.Collection;

public final class PlaylistFactory {

    private PlaylistFactory() {
    }

    public static Playlist create(String title) {
        return new Playlist(title, false);
    }

    public static Playlist createFavoritePlaylist(String title) {
        return new Playlist(title, true);
    }

    public static Playlist create(String title, boolean favorite) {
        return new Playlist(title, favorite);
    }

    public static Playlist create(String title, Collection<Track> tracks) {
        Playlist playlist = new Playlist(title, false);

        if (tracks != null) {
            tracks.forEach(playlist::addTrack);
        }

        return playlist;
    }

    public static Playlist create(String title, boolean favorite, Collection<Track> tracks) {
        Playlist playlist = new Playlist(title, favorite);

        if (tracks != null) {
            tracks.forEach(playlist::addTrack);
        }

        return playlist;
    }

    public static Playlist copyOf(Playlist original) {
        Playlist copy = new Playlist(
                original.getTitle(),
                original.isFavorite()
        );

        original.getTracks().forEach(copy::addTrack);

        return copy;
    }

    public static Playlist fromTrack(String title, Track track) {
        Playlist playlist = new Playlist(title, false);

        if (track != null) {
            playlist.addTrack(track);
        }

        return playlist;
    }

    public static Playlist emptyFavorites() {
        return new Playlist("Favorites", true);
    }
}