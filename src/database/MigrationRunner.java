package database;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Lightweight SQL migration runner (school-friendly).
public class MigrationRunner {
    private static final String MIGRATIONS_DIR = "migrations";

    private MigrationRunner() {
    }

    public static void migrate() {
        try (Connection conn = DBConnection.getConnection()) {
            ensureMigrationsTable(conn);
            List<Path> migrations = listSqlFiles();
            for (Path file : migrations) {
                String name = file.getFileName().toString();
                if (isApplied(conn, name)) {
                    continue;
                }
                applyFile(conn, name, file);
                markApplied(conn, name);
            }
        } catch (Exception e) {
            // Fail fast: schema must be correct for the app to work.
            throw new RuntimeException("Database migration failed: " + e.getMessage(), e);
        }
    }

    private static void ensureMigrationsTable(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute(
                    "CREATE TABLE IF NOT EXISTS schema_migrations (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY," +
                            "migration VARCHAR(200) NOT NULL UNIQUE," +
                            "applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                            ")"
            );
        }
    }

    private static List<Path> listSqlFiles() throws IOException {
        Path dir = Paths.get(MIGRATIONS_DIR);
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            return new ArrayList<>();
        }
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.sql")) {
            for (Path p : stream) {
                files.add(p);
            }
        }
        files.sort(Comparator.comparing(p -> p.getFileName().toString()));
        return files;
    }

    private static boolean isApplied(Connection conn, String migrationName) throws SQLException {
        String sql = "SELECT 1 FROM schema_migrations WHERE migration = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, migrationName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void markApplied(Connection conn, String migrationName) throws SQLException {
        String sql = "INSERT INTO schema_migrations (migration) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, migrationName);
            ps.executeUpdate();
        }
    }

    private static void applyFile(Connection conn, String migrationName, Path file) throws IOException, SQLException {
        String sqlText = Files.readString(file, StandardCharsets.UTF_8);
        List<String> statements = splitStatements(sqlText);
        conn.setAutoCommit(false);
        try (Statement st = conn.createStatement()) {
            for (String stmt : statements) {
                String trimmed = stmt.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                try {
                    st.execute(trimmed);
                } catch (SQLException ex) {
                    // Ignore idempotency-ish errors so re-runs don't brick a demo DB.
                    if (isIgnorableMigrationError(ex)) {
                        continue;
                    }
                    throw new SQLException("Migration " + migrationName + " failed: " + ex.getMessage(), ex);
                }
            }
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private static boolean isIgnorableMigrationError(SQLException ex) {
        // MySQL common codes:
        // 1050: Table already exists
        // 1060: Duplicate column name
        // 1061: Duplicate key name
        // 1091: Can't DROP ... check that column/key exists
        int code = ex.getErrorCode();
        return code == 1050 || code == 1060 || code == 1061 || code == 1091;
    }

    private static List<String> splitStatements(String sqlText) {
        // Minimal splitter: removes "--" line comments and splits by ';'
        String[] lines = sqlText.replace("\r\n", "\n").split("\n");
        StringBuilder cleaned = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                continue;
            }
            int idx = line.indexOf("--");
            if (idx >= 0) {
                line = line.substring(0, idx);
            }
            cleaned.append(line).append('\n');
        }

        List<String> out = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            if (c == '\'') {
                inSingleQuote = !inSingleQuote;
            }
            if (c == ';' && !inSingleQuote) {
                out.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (current.toString().trim().length() > 0) {
            out.add(current.toString());
        }
        return out;
    }
}

