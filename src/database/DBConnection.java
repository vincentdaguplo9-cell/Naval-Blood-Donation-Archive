package database;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

// Centralized JDBC connection utility (config-based).
public class DBConnection {
    private static final String CONFIG_PATH = "config/db.properties";
    private static String URL;
    private static String USER;
    private static String PASS;

    static {
        try {
            // Load MySQL JDBC driver.
            Class.forName("com.mysql.cj.jdbc.Driver");
            loadConfig();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found.", e);
        } catch (IOException e) {
            throw new RuntimeException("Database config not found. Expected: " + CONFIG_PATH, e);
        }
    }

    // Load DB settings from config/db.properties so it can be edited anytime.
    private static void loadConfig() throws IOException {
        Path path = Paths.get(CONFIG_PATH);
        if (!Files.exists(path)) {
            throw new IOException("Missing config file: " + CONFIG_PATH);
        }
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(path.toFile())) {
            props.load(in);
        }
        URL = props.getProperty("db.url");
        USER = props.getProperty("db.user");
        PASS = props.getProperty("db.pass");
        if (URL == null || USER == null || PASS == null) {
            throw new IOException("Config file must contain db.url, db.user, db.pass");
        }
    }

    // Get a new database connection.
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
