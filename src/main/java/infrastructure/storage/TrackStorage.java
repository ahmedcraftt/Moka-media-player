package infrastructure.storage;

import application.dto.TrackDTO;
import domain.model.Track;
import infrastructure.mapper.TrackMapper;

import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrackStorage {
    public void initialize() {
        String sql = """
                CREATE TABLE IF NOT EXISTS tracks (
                path Text,PRIMARY KEY,
                title TEXT NOT NULL,
                favorite INTEGER NOT NULL,
                times_played INTEGER NOT NULL
                );
                """;

        try (
                Connection connection = DatabaseManager.connect();
                Statement stmt = connection.createStatement()
        ) {
            stmt.execute(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void save(Track track) {
        TrackDTO dto = TrackMapper.toDTO(track);

        String sql = """
                INSERT INTO
                tracks(title, favorite, times_played,path)
                VALUES(?, ?, ?, ?)
                """;
        try (
                Connection connection = DatabaseManager.connect();
                PreparedStatement stmt = connection.prepareStatement(sql)
        ) {
            stmt.setString(1, dto.getTitle());
            stmt.setBoolean(2, dto.isFavorite());
            stmt.setInt(3, dto.getTimesPlayed());
            stmt.setString(4, dto.getPath());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Track> loadTracks() {
        List<Track> tracks = new ArrayList<>();

        String sql = "SELECT * FROM tracks";

        try (
                Connection connection = DatabaseManager.connect();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)
        ) {
            while (resultSet.next()) {
                TrackDTO dto = new TrackDTO();
                dto.setTitle(resultSet.getString("title"));
                dto.setFavorite(resultSet.getBoolean("favorite"));
                dto.setTimesPlayed(resultSet.getInt("times_played"));
                dto.setPath(resultSet.getString("path"));
                Track track = TrackMapper.fromDTO(dto);
                tracks.add(track);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tracks;
    }

    public void update(Track track) {
        TrackDTO dto = TrackMapper.toDTO(track);

        String sql = """
                UPDATE tracks
                SET title = ?,
                    favorite = ?,
                    times_played = ?
                WHERE path = ?
                """;

        try (
                Connection conn = DatabaseManager.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, dto.getTitle());
            stmt.setBoolean(2, dto.isFavorite());
            stmt.setInt(3, dto.getTimesPlayed());
            stmt.setString(4, dto.getPath());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Path path) {

        String sql = "DELETE FROM tracks WHERE path = ?";

        try (
                Connection conn = DatabaseManager.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, path.toString());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
