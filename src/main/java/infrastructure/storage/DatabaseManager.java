package infrastructure.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    private static final Path dbPath;

    static {
        try {
            dbPath = AppDirectories.getDataDirectory();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String URL;

    static {
        if (Boolean.getBoolean("moka.dev")) {
            URL = "jdbc:sqlite:moka.db";
        } else {
            URL = "jdbc:sqlite:" + dbPath.resolve("moka.db");
        }
    }

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }

    static {
        logger.info("Connecting to database at url: {} ", URL);
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}