package application.dto;

import java.util.List;

public record PlaylistDTO(String title, boolean favorite, List<String> trackPaths) {
}
