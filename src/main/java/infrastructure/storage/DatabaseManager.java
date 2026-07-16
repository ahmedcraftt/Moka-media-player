package infrastructure.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }

    private static final String URL = "jdbc:sqlite:moka.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}