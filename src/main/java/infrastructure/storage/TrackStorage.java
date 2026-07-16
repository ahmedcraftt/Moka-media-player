package infrastructure.storage;

import application.dto.TrackDTO;
import domain.model.media.TrackSyncState;
import domain.model.media.Track;
import domain.model.metadata.Metadata;
import infrastructure.mapper.TrackMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.*;
import java.util.List;

public class TrackStorage {

    private static final Logger logger = LoggerFactory.getLogger(TrackStorage.class);

    private static final int SCHEMA_VERSION = 3;

    private final MetadataStorage metadataStorage;

    public TrackStorage(MetadataStorage metadataStorage) {
        this.metadataStorage = metadataStorage;
    }

    public void initialize() {
        try (Connection connection = DatabaseManager.connect()) {

            createTableIfMissing(connection);

            migrate(connection);

        } catch (SQLException e) {
            logger.error("Failed to initialize Track storage.", e);
        }
    }

    public void save(Track track) {
        TrackDTO dto = TrackMapper.toDTO(track);

        String sql = """
                INSERT INTO tracks(metadata_id, favorite, times_played, path, media_type,
                                   last_modified, size, dateAdded,dateCreated,lastAccessed,fileType,last_played)
                           VALUES(?,?,?,?,?,?,?,?,?,?,?,?)
                           ON CONFLICT(path) DO UPDATE SET
                               metadata_id = excluded.metadata_id,
                               favorite = excluded.favorite,
                               times_played = excluded.times_played,
                               media_type = excluded.media_type,
                               last_modified = excluded.last_modified,
                               size = excluded.size,
                               dateAdded = excluded.dateAdded,
                               dateCreated = excluded.dateCreated,
                               lastAccessed = excluded.lastAccessed,
                               fileType = excluded.fileType,
                               last_played = excluded.last_played;
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
            logger.error("Failed to save track data for path: {}", dto.path(), e);
        }
    }

    public void saveAll(List<Track> tracks) {
        for (Track track : tracks) {
            save(track);
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
                        resultSet.getString("dateAdded"),
                        resultSet.getString("dateCreated"),
                        resultSet.getString("lastAccessed"),
                        resultSet.getString("fileType"),
                        resultSet.getString("last_played")
                );

                Metadata metadata = metadataStorage.load(dto.metadataId());

                Track track = TrackMapper.fromDTO(dto, metadata);

                if (track != null && track.getMetadata() != null) {
                    logger.debug("Loaded track metadata: {}", track.getMetadata().toText());
                }

                return track;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading track from path: " + path, e);
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
                    dateAdded = ?,
                    dateCreated = ?,
                    lastAccessed = ?,
                    fileType = ?,
                    last_played = ?
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
            statement.setString(8, dto.dateCreated());
            statement.setString(9, dto.lastAccessed());
            statement.setString(10, dto.fileType());
            statement.setString(11, dto.lastPlayed());
            statement.setString(12, dto.path());
            metadataStorage.update(track.getMetadata());

            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to update track record at path: {}", dto.path(), e);
        }
    }

    public void UpdateAll(List<Track> tracks) {
        for (Track track : tracks) {
            update(track);
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
            logger.error("Failed to delete track record at path: {}", path, e);
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

    private void mapDtoToStatement(PreparedStatement statement, TrackDTO dto)
            throws SQLException {
        statement.setInt(1, dto.metadataId());
        statement.setInt(2, dto.favorite() ? 1 : 0);
        statement.setInt(3, dto.timesPlayed());
        statement.setString(4, dto.path());
        statement.setString(5, dto.type());
        statement.setLong(6, dto.lastModified());
        statement.setLong(7, dto.size());
        statement.setString(8, dto.dateAdded());
        statement.setString(9, dto.dateCreated());
        statement.setString(10, dto.lastAccessed());
        statement.setString(11, dto.fileType());
        statement.setString(12, dto.lastPlayed());
    }

    private void createTableIfMissing(Connection connection) throws SQLException {

        String sql = """
                CREATE TABLE IF NOT EXISTS tracks (
                    path TEXT PRIMARY KEY,
                    metadata_id INTEGER,
                    favorite INTEGER NOT NULL,
                    times_played INTEGER NOT NULL,
                    media_type TEXT NOT NULL,
                    last_modified INTEGER NOT NULL,
                    size INTEGER NOT NULL,
                    dateAdded TEXT NOT NULL,
                    dateCreated TEXT NOT NULL,
                    lastAccessed TEXT NOT NULL,
                    fileType TEXT NOT NULL,
                    last_played TEXT,
                    FOREIGN KEY(metadata_id)
                        REFERENCES metadata(metadata_id)
                        ON DELETE SET NULL
                );
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void migrate(Connection connection) throws SQLException {

        int version = getSchemaVersion(connection);

        while (version < SCHEMA_VERSION) {

            switch (version) {

                case 0 -> migrateToVersion1(connection);

                case 1 -> migrateToVersion2(connection);

                case 2 -> migrateToVersion3(connection);

                default -> throw new IllegalStateException(
                        "Unknown database version: " + version);

            }

            version++;
            setSchemaVersion(connection, version);
        }
    }

    private int getSchemaVersion(Connection connection) throws SQLException {

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA user_version")) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void setSchemaVersion(Connection connection, int version)
            throws SQLException {

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA user_version = " + version);
        }
    }

    private void migrateToVersion1(Connection connection) {

        logger.info("Migrating database -> Version 1");

    }

    private void migrateToVersion2(Connection connection)
            throws SQLException {

        logger.info("Migrating database -> Version 2");

        executeIfMissing(connection,
                "dateCreated",
                "ALTER TABLE tracks ADD COLUMN dateCreated TEXT NOT NULL DEFAULT ''");

        executeIfMissing(connection,
                "lastAccessed",
                "ALTER TABLE tracks ADD COLUMN lastAccessed TEXT NOT NULL DEFAULT ''");

        executeIfMissing(connection,
                "fileType",
                "ALTER TABLE tracks ADD COLUMN fileType TEXT NOT NULL DEFAULT ''");
    }

    private void migrateToVersion3(Connection connection) throws SQLException {
        logger.info("Migrating database -> Version 3");

        executeIfMissing(
                connection,
                "last_played",
                "ALTER TABLE tracks ADD COLUMN last_played TEXT ''"
        );

    }

    private void executeIfMissing(Connection connection,
                                  String column,
                                  String sql)
            throws SQLException {

        if (hasColumn(connection, column))
            return;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }

        logger.info("Added '{}' column.", column);
    }

    private boolean hasColumn(Connection connection,
                              String column)
            throws SQLException {

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(tracks)")) {

            while (rs.next()) {

                if (column.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        }

        return false;
    }
}