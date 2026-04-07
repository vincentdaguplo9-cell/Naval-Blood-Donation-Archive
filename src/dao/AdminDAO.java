package dao;

import database.DBConnection;

import util.SecurityUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

// Data access for admin login.
public class AdminDAO {
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;

    public LoginResult authenticate(String username, String password) {
        String sql = "SELECT admin_id, password, password_hash, password_salt, failed_attempts, lock_until " +
                "FROM admin_table WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return LoginResult.invalid("Invalid username or password.");
                }

                int adminId = rs.getInt("admin_id");
                String legacyPassword = rs.getString("password");
                String hash = rs.getString("password_hash");
                String salt = rs.getString("password_salt");
                int failedAttempts = rs.getInt("failed_attempts");
                Timestamp lockUntil = rs.getTimestamp("lock_until");

                if (lockUntil != null && lockUntil.toLocalDateTime().isAfter(LocalDateTime.now())) {
                    return LoginResult.locked("Account locked until " + lockUntil.toLocalDateTime().toString().replace('T', ' ') + ".");
                }

                boolean valid;
                if (hash != null && salt != null) {
                    valid = SecurityUtil.verifyPassword(password, salt, hash);
                } else {
                    valid = legacyPassword != null && legacyPassword.equals(password);
                    if (valid) {
                        upgradeLegacyPassword(conn, adminId, password);
                    }
                }

                if (valid) {
                    resetLockout(conn, adminId);
                    return LoginResult.success();
                }

                failedAttempts++;
                if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                    lockAccount(conn, adminId);
                    return LoginResult.locked("Too many failed attempts. Try again in " + LOCK_MINUTES + " minutes.");
                }

                updateFailedAttempts(conn, adminId, failedAttempts);
                return LoginResult.invalid("Invalid username or password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unknown column")) {
                return legacyValidate(username, password);
            }
            return LoginResult.error("Login failed due to a database error.");
        }
    }

    private void upgradeLegacyPassword(Connection conn, int adminId, String password) throws SQLException {
        String salt = SecurityUtil.generateSalt();
        String hash = SecurityUtil.hashPassword(password, salt);
        String sql = "UPDATE admin_table SET password_hash = ?, password_salt = ?, password = NULL WHERE admin_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setString(2, salt);
            ps.setInt(3, adminId);
            ps.executeUpdate();
        }
    }

    private void resetLockout(Connection conn, int adminId) throws SQLException {
        String sql = "UPDATE admin_table SET failed_attempts = 0, lock_until = NULL WHERE admin_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.executeUpdate();
        }
    }

    private void updateFailedAttempts(Connection conn, int adminId, int failedAttempts) throws SQLException {
        String sql = "UPDATE admin_table SET failed_attempts = ? WHERE admin_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, failedAttempts);
            ps.setInt(2, adminId);
            ps.executeUpdate();
        }
    }

    private void lockAccount(Connection conn, int adminId) throws SQLException {
        LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
        String sql = "UPDATE admin_table SET failed_attempts = 0, lock_until = ? WHERE admin_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(lockUntil));
            ps.setInt(2, adminId);
            ps.executeUpdate();
        }
    }

    private LoginResult legacyValidate(String username, String password) {
        String sql = "SELECT 1 FROM admin_table WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? LoginResult.success() : LoginResult.invalid("Invalid username or password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return LoginResult.error("Login failed due to a database error.");
        }
    }

    public static class LoginResult {
        public final boolean success;
        public final String message;
        public final Status status;

        private LoginResult(boolean success, Status status, String message) {
            this.success = success;
            this.status = status;
            this.message = message;
        }

        public static LoginResult success() {
            return new LoginResult(true, Status.SUCCESS, "Login successful.");
        }

        public static LoginResult invalid(String message) {
            return new LoginResult(false, Status.INVALID, message);
        }

        public static LoginResult locked(String message) {
            return new LoginResult(false, Status.LOCKED, message);
        }

        public static LoginResult error(String message) {
            return new LoginResult(false, Status.ERROR, message);
        }
    }

    public enum Status {
        SUCCESS,
        INVALID,
        LOCKED,
        ERROR
    }
}
