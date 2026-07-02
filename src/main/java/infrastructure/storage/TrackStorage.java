package infrastructure.storage;

import application.dto.TrackDTO;
import domain.model.media.TrackSyncState;
import domain.model.media.Track;
import domain.model.metadata.Metadata;
import infrastructure.mapper.TrackMapper;

import java.nio.file.Path;
import java.sql.*;
import java.util.List;

public class TrackStorage {

    private final MetadataStorage metadataStorage;

    public TrackStorage(MetadataStorage metadataStorage) {
        this.metadataStorage = metadataStorage;
    }

    public void initialize() {
        String createTableSql = """
                CREATE TABLE IF NOT EXISTS tracks (
                path TEXT PRIMARY KEY,
                metadata_id INTEGER,
                favorite INTEGER NOT NULL,
                times_played INTEGER NOT NULL,
                media_type TEXT NOT NULL,
                last_modified INTEGER NOT NULL,
                size INTEGER NOT NULL,
                dateAdded TEXT NOT NULL,
                FOREIGN KEY(metadata_id) REFERENCES metadata(metadata_id) ON DELETE SET NULL
                );
                """;

        String validationSql = "SELECT path, metadata_id, favorite, times_played FROM tracks LIMIT 0";

        try (Connection connection = DatabaseManager.connect()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(createTableSql);
            }

            try (Statement statement = connection.createStatement()) {
                statement.executeQuery(validationSql);
            } catch (SQLException e) {
                System.err.println("WARNING: 'tracks' table layout is outdated or corrupted. Re-initializing table...");

                try (Statement statement = connection.createStatement()) {
                    statement.execute("DROP TABLE IF EXISTS tracks;");
                    statement.execute(createTableSql);
                    System.out.println("SUCCESS: 'tracks' table successfully recreated.");
                }
            }
        } catch (SQLException e) {
            System.err.println("CRITICAL: Failed to initialize Track storage subsystem.");
            e.printStackTrace();
        }
    }

    public void save(Track track) {
        TrackDTO dto = TrackMapper.toDTO(track);

        String sql = """
                INSERT INTO tracks(metadata_id, favorite, times_played, path, media_type, last_modified, size, dateAdded)
                           VALUES(?,?,?,?,?,?,?,?)
                           ON CONFLICT(path) DO UPDATE SET
                               metadata_id = excluded.metadata_id,
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
            connection.setAutoCommit(false);
            mapDtoToStatement(statement, dto);
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveAll(List<Track> tracks) {
        String sql = """
                INSERT INTO tracks(metadata_id, favorite, times_played, path, media_type, last_modified, size, dateAdded)
                           VALUES(?,?,?,?,?,?,?,?)
                           ON CONFLICT(path) DO UPDATE SET
                               metadata_id = excluded.metadata_id,
                               favorite = excluded.favorite,
                               times_played = excluded.times_played,
                               media_type = excluded.media_type,
                               last_modified = excluded.last_modified,
                               size = excluded.size,
                               dateAdded = excluded.dateAdded;
                """;

        try (Connection connection = DatabaseManager.connect()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (Track track : tracks) {
                    TrackDTO dto = TrackMapper.toDTO(track);
                    mapDtoToStatement(statement, dto);
                    statement.addBatch();
                }
                statement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Track load(String path) {
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
                        resultSet.getInt("metadata_id"),
                        resultSet.getInt("favorite") == 1,
                        resultSet.getInt("times_played"),
                        resultSet.getString("path"),
                        resultSet.getString("media_type"),
                        resultSet.getLong("last_modified"),
                        resultSet.getLong("size"),
                        resultSet.getString("dateAdded")
                );


                Metadata metadata = metadataStorage.load(dto.metadataId());
                Track track = TrackMapper.fromDTO(dto, metadata);
                IO.println("TrackStorage.java line 152 Track load(String path):" + track.getMetadata().toText());
                return track;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(Track track) {
        TrackDTO dto = TrackMapper.toDTO(track);

        String sql = """
                UPDATE tracks
                SET metadata_id = ?,
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
            statement.setInt(1, dto.metadataId());
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
                SELECT last_modified, size, media_type
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
            throw new RuntimeException("Cannot read last modified time for " + path, e);
        }
    }

    private void mapDtoToStatement(PreparedStatement statement, TrackDTO dto) throws SQLException {
        statement.setInt(1, dto.metadataId());
        statement.setInt(2, dto.favorite() ? 1 : 0);
        statement.setInt(3, dto.timesPlayed());
        statement.setString(4, dto.path());
        statement.setString(5, dto.type());
        statement.setLong(6, dto.lastModified());
        statement.setLong(7, dto.size());
        statement.setString(8, dto.dateAdded());
    }
}