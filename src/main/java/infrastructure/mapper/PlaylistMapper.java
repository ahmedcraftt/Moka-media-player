package infrastructure.mapper;

import domain.model.Playlist;
import application.dto.PlaylistDTO;
import domain.library.MediaLibrary;
import infrastructure.factory.PlaylistFactory;

import static java.util.Arrays.stream;

public class PlaylistMapper {

    public static PlaylistDTO toDTO(Playlist p) {

        return new PlaylistDTO(p.getTitle(), p.isFavorite(), p.getTracks().stream()
                .map(t -> t.getFiledata().getFilePath().toString())
                .toList());
    }

    public static Playlist fromDTO(PlaylistDTO dto, MediaLibrary library) {
        Playlist p = PlaylistFactory.create(dto.title(), dto.favorite());

        for (String path : dto.trackPaths()) {
            library.getTracks().stream()
                    .filter(t -> t.getFiledata().getFilePath().toString().equals(path))
                    .findFirst()
                    .ifPresent(p::addTrack);
        }

        return p;
    }
}
