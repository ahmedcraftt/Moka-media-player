package infrastructure.storage;

import application.dto.TrackDTO;
import domain.model.media.TrackSyncState;
import domain.model.media.Track;
import infrastructure.mapper.TrackMapper;

import java.nio.file.Path;
import java.sql.*;
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
                size INTEGER NOT NULL,
                dateAdded Text NOT NULL
                );
                """;

        try (
                Connection connection = DatabaseManager.connect();
                Statement statement = connection.createStatement()
        ) {
            statement.execute(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void save(Track track) {
        TrackDTO dto = TrackMapper.toDTO(track);

        String sql = """
                INSERT INTO tracks(title, favorite, times_played, path, media_type, last_modified, size, dateAdded)
                          VALUES(?,?,?,?,?,?,?,?)
                          ON CONFLICT(path) DO UPDATE SET
                              title = excluded.title,
                              favorite = excluded.favorite,
                              times_played = excluded.times_played,
                              media_type = excluded.media_type,
                              last_modified = excluded.last_modified,
                              size = excluded.size,
                              dateAdded = excluded.dateAdded;
                """;
        try (
                Connection connection = DatabaseManager.connect();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, dto.title());
            statement.setInt(2, dto.favorite() ? 1 : 0);
            statement.setInt(3, dto.timesPlayed());
            statement.setString(4, dto.path());
            statement.setString(5, dto.type());
            statement.setLong(6, dto.lastModified());
            statement.setLong(7, dto.size());
            statement.setString(8, dto.dateAdded());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveAll(List<Track> tracks) {
        for (Track track : tracks) {
            save(track);
        }
    }

    public Track loadTrack(String path) {
        String sql = "SELECT * FROM tracks WHERE path = ?";

        try (
                Connection connection = DatabaseManager.connect();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, path);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return null;
                }

                TrackDTO dto = new TrackDTO(
                        resultSet.getString("title"),
                        resultSet.getInt("favorite") == 1,
                        resultSet.getInt("times_played"),
                        resultSet.getString("path"),
                        resultSet.getString("media_type"),
                        resultSet.getLong("last_modified"),
                        resultSet.getLong("size"),
                        resultSet.getString("dateAdded")
                );

                return TrackMapper.fromDTO(dto);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
                    size = ?,
                    dateAdded = ?
                WHERE path = ?
                """;

        try (
                Connection connection = DatabaseManager.connect();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, dto.title());
            statement.setInt(2, dto.favorite() ? 1 : 0);
            statement.setInt(3, dto.timesPlayed());
            statement.setString(4, dto.type());
            statement.setLong(5, dto.lastModified());
            statement.setLong(6, dto.size());
            statement.setString(7, dto.dateAdded());
            statement.setString(8, dto.path());

            statement.executeUpdate();

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

    public TrackSyncState getState(Path path) {

        String sql = """
                SELECT last_modified, size ,media_type
                FROM tracks
                WHERE path = ?
                """;

        try (
                Connection connection = DatabaseManager.connect();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, path.toString());

            try (ResultSet rs = statement.executeQuery()) {

                if (!rs.next()) {
                    return new TrackSyncState(false, 0, 0, "");
                }

                return new TrackSyncState(
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
