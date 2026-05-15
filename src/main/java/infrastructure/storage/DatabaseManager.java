package infrastructure.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:tracks.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}