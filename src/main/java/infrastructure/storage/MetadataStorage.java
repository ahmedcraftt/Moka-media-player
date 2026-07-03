package infrastructure.storage;

import application.dto.MetadataDTO;
import domain.model.metadata.Metadata;
import infrastructure.mapper.MetadataMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class MetadataStorage {

    private static final Logger logger = LoggerFactory.getLogger(MetadataStorage.class);

    public void initialize() {
        String createTableSql = """
                CREATE TABLE IF NOT EXISTS metadata (
                metadata_id INTEGER PRIMARY KEY AUTOINCREMENT,
                duration_in_seconds INTEGER NOT NULL,
                bitrate INTEGER NOT NULL,
                samplerate INTEGER NOT NULL,
                title TEXT NOT NULL,
                genre TEXT NOT NULL,
                description TEXT NOT NULL,
                lyrics TEXT NOT NULL,
                language TEXT NOT NULL,
                year INTEGER NOT NULL,
                artwork_path TEXT NOT NULL,
                series TEXT NOT NULL,
                artist TEXT NOT NULL,
                series_artist TEXT NOT NULL,
                track_number INTEGER NOT NULL
                );
                """;

        String validationSql = "SELECT metadata_id, duration_in_seconds, title FROM metadata LIMIT 0";

        try (Connection connection = DatabaseManager.connect()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(createTableSql);
            }

            try (Statement statement = connection.createStatement()) {
                statement.executeQuery(validationSql);
            } catch (SQLException e) {
                logger.warn("'metadata' table layout is outdated or corrupted. Re-initializing table...", e);

                try (Statement statement = connection.createStatement()) {
                    statement.execute("DROP TABLE IF EXISTS metadata;");
                    statement.execute(createTableSql);
                    logger.info("'metadata' table successfully recreated.");
                }
            }
        } catch (SQLException e) {
            // 3. Replaced critical err print stream with an error log level
            logger.error("CRITICAL: Failed to initialize Metadata storage subsystem.", e);
        }
    }

    public void save(Metadata metadata) {
        MetadataDTO dto = MetadataMapper.toDTO(metadata);

        String sql = """
                 INSERT INTO metadata(
                 duration_in_seconds, bitrate, samplerate, title, genre,
                 description, lyrics, language, year, artwork_path,
                 series, artist, series_artist, track_number)
                 VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;

        try (
                Connection connection = DatabaseManager.connect();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            mapDtoToStatement(statement, dto);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to save metadata for track title: '{}'", dto.title(), e);
        }
    }

    public void saveAll(List<Metadata> metadataList) {
        String sql = """
                 INSERT INTO metadata(
                 duration_in_seconds, bitrate, samplerate, title, genre,
                 description, lyrics, language, year, artwork_path,
                 series, artist, series_artist, track_number)
                 VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;

        try (Connection connection = DatabaseManager.connect()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (Metadata metadata : metadataList) {
                    MetadataDTO dto = MetadataMapper.toDTO(metadata);
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
            logger.error("Failed to execute batch metadata save operation.", e);
        }
    }

    public int saveAndGetId(Metadata metadata) {
        MetadataDTO dto = MetadataMapper.toDTO(metadata);
        String sql = """
                 INSERT INTO metadata(duration_in_seconds, bitrate, samplerate, title, genre,\s
                                      description, lyrics, language, year, artwork_path,\s
                                      series, artist, series_artist, track_number)
                 VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;
        try (
                Connection connection = DatabaseManager.connect();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            connection.setAutoCommit(false);
            mapDtoToStatement(statement, dto);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    logger.debug("Metadata identity mapping verified: domain ID = {}, generated DB ID = {}",
                            metadata.getId(), generatedKeys.getInt(1));
                    connection.commit();
                    return generatedKeys.getInt(1);
                }
            }

        } catch (SQLException e) {
            logger.error("Database error occurred while running identity save for track: '{}'", dto.title(), e);
        }
        throw new RuntimeException("Failed to save track metadata record; identity generation failed.");
    }

    public Metadata load(int id) {
        String sql = "SELECT * FROM metadata WHERE metadata_id = ?";

        try (
                Connection connection = DatabaseManager.connect();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    MetadataDTO dto = new MetadataDTO(
                            rs.getInt("metadata_id"),
                            rs.getInt("duration_in_seconds"),
                            rs.getLong("bitrate"),
                            rs.getLong("samplerate"),
                            rs.getString("title"),
                            rs.getString("genre"),
                            rs.getString("description"),
                            rs.getString("lyrics"),
                            rs.getString("language"),
                            rs.getInt("year"),
                            rs.getString("artwork_path"),
                            rs.getString("series"),
                            rs.getString("artist"),
                            rs.getString("series_artist"),
                            rs.getInt("track_number")
                    );

                    Metadata metadata = MetadataMapper.fromDTO(dto);

                    if (metadata != null) {
                        logger.debug("Successfully loaded metadata ID {}: {}", id, metadata.toText());
                    }

                    return metadata;
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load metadata record with ID: {}", id, e);
        }
        return null;
    }

    public void update(Metadata metadata) {
        MetadataDTO dto = MetadataMapper.toDTO(metadata);

        String sql = """
                 UPDATE metadata SET
                 duration_in_seconds = ?, bitrate = ?, samplerate = ?, title = ?, genre = ?,\s
                 description = ?, lyrics = ?, language = ?, year = ?, artwork_path = ?,\s
                 series = ?, artist = ?, series_artist = ?, track_number = ?
                 WHERE metadata_id = ?
                """;

        try (
                Connection connection = DatabaseManager.connect();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            mapDtoToStatement(statement, dto);
            statement.setInt(15, dto.id());

            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to update metadata record with ID: {}", dto.id(), e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM metadata WHERE metadata_id = ?";

        try (
                Connection connection = DatabaseManager.connect();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to delete metadata record with ID: {}", id, e);
        }
    }

    private void mapDtoToStatement(PreparedStatement statement, MetadataDTO dto) throws SQLException {
        statement.setInt(1, dto.durationInSeconds());
        statement.setLong(2, dto.bitrate());
        statement.setLong(3, dto.samplerate());
        statement.setString(4, dto.title());
        statement.setString(5, dto.genre());
        statement.setString(6, dto.description());
        statement.setString(7, dto.lyrics());
        statement.setString(8, dto.language());
        statement.setInt(9, dto.year());
        statement.setString(10, dto.artworkPath());
        statement.setString(11, dto.series());
        statement.setString(12, dto.artist());
        statement.setString(13, dto.seriesArtist());
        statement.setInt(14, dto.trackNumber());
    }
}