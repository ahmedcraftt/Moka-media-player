package infrastructure.mapper;

import domain.model.Playlist;
import application.dto.PlaylistDTO;
import domain.library.MediaLibrary;
import infrastructure.factory.PlaylistFactory;

public class PlaylistMapper {

    public static PlaylistDTO toDTO(Playlist p) {
        PlaylistDTO dto = new PlaylistDTO();
        dto.setTitle(p.getTitle());
        dto.setFavorite(p.isFavorite());
        dto.setTrackPaths(p.getTracks()
                .stream()
                .map(t -> t.getFilePath().toString())
                .toList());
        return dto;
    }

    public static Playlist fromDTO(PlaylistDTO dto, MediaLibrary library) {
        Playlist p = PlaylistFactory.create(dto.getTitle(), dto.isFavorite());

        for (String path : dto.getTrackPaths()) {
            library.getTracks().stream()
                    .filter(t -> t.getFilePath().toString().equals(path))
                    .findFirst()
                    .ifPresent(p::addTrack);
        }

        return p;
    }
}
