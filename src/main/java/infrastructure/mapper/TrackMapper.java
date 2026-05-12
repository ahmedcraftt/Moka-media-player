package infrastructure.mapper;

import application.dto.TrackDTO;
import domain.model.Track;
import infrastructure.factory.TrackFactory;

public class TrackMapper {

    public static TrackDTO toDTO(Track track) {
        TrackDTO dto = new TrackDTO();
        dto.setTitle(track.getTitle());
        dto.setFavorite(track.isFavorite());
        dto.setPath(track.getFilePath());
        return dto;
    }

    public static Track fromDTO(TrackDTO dto) {
        Track track = TrackFactory.create(dto.getPath());
        track.setFavorite(dto.isFavorite());
        track.setTitle(dto.getTitle());
        return track;
    }

}
