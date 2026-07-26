package infrastructure.mapper;

import domain.model.media.Playlist;
import application.dto.PlaylistDTO;
import domain.model.library.MediaLibrary;

public class PlaylistMapper {

    public static PlaylistDTO toDTO(Playlist p) {

        return new PlaylistDTO(p.getTitle(), p.isFavorite(), p.getTracks().stream()
                .map(t -> t.getFiledata().getFilePath().toString())
                .toList());
    }

    public static Playlist fromDTO(PlaylistDTO dto, MediaLibrary library) {
        Playlist p = new Playlist(dto.title(), dto.favorite());

        for (String path : dto.trackPaths()) {
            library.getTracks().stream()
                    .filter(t -> t.getFiledata().getFilePath().toString().equals(path))
                    .findFirst()
                    .ifPresent(p::addTrack);
        }

        return p;
    }
}
