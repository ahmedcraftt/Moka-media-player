package infrastructure.storage;

import application.dto.TrackDTO;
import domain.model.TrackState;
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
                path Text PRIMARY KEY,
                title TEXT NOT NULL,
                favorite INTEGER NOT NULL,
                times_played INTEGER NOT NULL,
                media_type TEXT NOT NULL,
                last_modified INTEGER NOT NULL,
                size INTEGER NOT NULL
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
                INSERT INTO tracks(title, favorite, times_played, path, media_type, last_modified, size)
                          VALUES(?,?,?,?,?,?,?)
                          ON CONFLICT(path) DO UPDATE SET
                              title = excluded.title,
                              favorite = excluded.favorite,
                              times_played = excluded.times_played,
                              media_type = excluded.media_type,
                              last_modified = excluded.last_modified,
                              size = excluded.size;
                """;
        try (
                Connection connection = DatabaseManager.connect();
                PreparedStatement stmt = connection.prepareStatement(sql)
        ) {
            stmt.setString(1, dto.title());
            stmt.setInt(2, dto.favorite() ? 1 : 0);
            stmt.setInt(3, dto.timesPlayed());
            stmt.setString(4, dto.path());
            stmt.setString(5, dto.type());
            stmt.setLong(6, dto.lastModified());
            stmt.setLong(7, dto.size());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveAll(List<Track> tracks) {
        for (Track track : tracks) {
            save(track);
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
                TrackDTO dto = new TrackDTO(
                        resultSet.getString("title"),
                        resultSet.getInt("favorite") == 1,
                        resultSet.getInt("times_played"),
                        resultSet.getString("path"),
                        resultSet.getString("media_type"),
                        resultSet.getInt("last_modified"),
                        resultSet.getInt("size")
                );
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
                    times_played = ?,
                    media_type = ?,
                    last_modified = ?,
                    size = ?
                WHERE path = ?
                """;

        try (
                Connection conn = DatabaseManager.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, dto.title());
            stmt.setInt(2, dto.favorite() ? 1 : 0);
            stmt.setInt(3, dto.timesPlayed());
            stmt.setString(4, dto.type());
            stmt.setLong(5, dto.lastModified());
            stmt.setLong(6, dto.size());

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

    public TrackState getState(Path path) {

        String sql = """
                SELECT last_modified, size ,media_type
                FROM tracks
                WHERE path = ?
                """;

        try (
                Connection conn = DatabaseManager.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, path.toString());

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    return new TrackState(false, 0, 0, "");
                }

                return new TrackState(
                        true,
                        rs.getLong("last_modified"),
                        rs.getLong("size"),
                        rs.getString("media_type")
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
