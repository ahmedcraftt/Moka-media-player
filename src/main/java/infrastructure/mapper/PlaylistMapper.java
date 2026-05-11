package infrastructure.mapper;

import domain.model.Playlist;
import application.dto.PlaylistDTO;
import domain.library.MediaLibrary;

public class PlaylistMapper {

    public static PlaylistDTO toDTO(Playlist p) {
        PlaylistDTO dto = new PlaylistDTO();
        dto.title = p.getTitle();
        dto.favorite = p.isFavorite();
        dto.trackPaths = p.getTracks()
                .stream()
                .map(t -> t.getFilePath().toString())
                .toList();
        return dto;
    }

    public static Playlist fromDTO(PlaylistDTO dto, MediaLibrary library) {
        Playlist p = new Playlist(dto.title, dto.favorite);

        for (String path : dto.trackPaths) {
            library.getTracks().stream()
                    .filter(t -> t.getFilePath().toString().equals(path))
                    .findFirst()
                    .ifPresent(p::addTrack);
        }

        return p;
    }
}
