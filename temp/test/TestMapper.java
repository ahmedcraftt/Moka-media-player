package test;

import application.dto.TrackDTO;
import com.ahmed.utils.InputUtils;
import domain.model.media.MediaType;
import domain.model.media.Track;
import infrastructure.factory.TrackFactory;
import infrastructure.mapper.TrackMapper;

import java.nio.file.Path;
import java.time.LocalDate;

public final class TestMapper {
    static void main() {
        int test = InputUtils.readInt("test 1 or 2:");
        switch (test) {
            case 1:
                testFromDto();
                break;
            case 2:
                testToDto();
                break;
        }
    }

    public static void testFromDto() {
        TrackDTO dto = new TrackDTO("title", false, 2, "hiiii/hiii/hi.mp3", "song", 1234, 10, LocalDate.now().toString());
        Track track = TrackMapper.fromDTO(dto);
        IO.println(track.toText());
    }


    public static void testToDto() {
        Track track = TrackFactory.create("temp/test", true, 1, MediaType.SONG, Path.of("hii/hiiii/hi.mp3"), LocalDate.now());
        TrackDTO dto = TrackMapper.toDTO(track);
        IO.println(dto.toString());
    }
}
