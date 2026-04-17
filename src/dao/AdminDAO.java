package dao;

import database.DBConnection;

import util.SecurityUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
                    return LoginResult.success(adminId, username);
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
        String sql = "SELECT admin_id, username FROM admin_table WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return LoginResult.success(rs.getInt("admin_id"), rs.getString("username"));
                }
                return LoginResult.invalid("Invalid username or password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return LoginResult.error("Login failed due to a database error.");
        }
    }

    public ActionResult changePassword(int adminId, String currentPassword, String newPassword) {
        String sql = "SELECT password, password_hash, password_salt FROM admin_table WHERE admin_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return ActionResult.error("Admin account not found.");
                }

                String legacyPassword = rs.getString("password");
                String hash = rs.getString("password_hash");
                String salt = rs.getString("password_salt");

                boolean validCurrent;
                if (hash != null && salt != null) {
                    validCurrent = SecurityUtil.verifyPassword(currentPassword, salt, hash);
                } else {
                    validCurrent = legacyPassword != null && legacyPassword.equals(currentPassword);
                }

                if (!validCurrent) {
                    return ActionResult.error("Current password is incorrect.");
                }

                String newSalt = SecurityUtil.generateSalt();
                String newHash = SecurityUtil.hashPassword(newPassword, newSalt);
                String updateSql = "UPDATE admin_table SET password_hash = ?, password_salt = ?, password = NULL WHERE admin_id = ?";
                try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                    update.setString(1, newHash);
                    update.setString(2, newSalt);
                    update.setInt(3, adminId);
                    update.executeUpdate();
                }
                return ActionResult.success("Admin password updated.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return ActionResult.error("Failed to update admin password.");
        }
    }

    public ActionResult createStaffAccount(int staffId, String password) {
        String existsSql = "SELECT 1 FROM user_table WHERE user_id = ? OR username = ?";
        String insertSql = "INSERT INTO user_table (user_id, username, password_hash, password_salt, role, full_name, active) " +
                "VALUES (?, ?, ?, ?, 'STAFF', ?, 1)";
        String username = String.valueOf(staffId);
        String fullName = "Staff " + staffId;
        String salt = SecurityUtil.generateSalt();
        String hash = SecurityUtil.hashPassword(password, salt);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement exists = conn.prepareStatement(existsSql)) {
            exists.setInt(1, staffId);
            exists.setString(2, username);
            try (ResultSet rs = exists.executeQuery()) {
                if (rs.next()) {
                    return ActionResult.error("Staff account already exists for ID " + staffId + ".");
                }
            }

            try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                insert.setInt(1, staffId);
                insert.setString(2, username);
                insert.setString(3, hash);
                insert.setString(4, salt);
                insert.setString(5, fullName);
                insert.executeUpdate();
            }
            return ActionResult.success("Staff account created for ID " + staffId + ".");
        } catch (SQLException e) {
            e.printStackTrace();
            return ActionResult.error("Failed to create staff account.");
        }
    }

    public List<StaffAccount> getStaffAccounts() {
        List<StaffAccount> accounts = new ArrayList<>();
        String sql = "SELECT user_id, username, full_name, active, created_at FROM user_table WHERE role = 'STAFF' ORDER BY user_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                accounts.add(new StaffAccount(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getBoolean("active"),
                        rs.getTimestamp("created_at") == null ? "" : rs.getTimestamp("created_at").toString()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
    }

    public static class LoginResult {
        public final boolean success;
        public final String message;
        public final Status status;
        public final int adminId;
        public final String username;

        private LoginResult(boolean success, Status status, String message, int adminId, String username) {
            this.success = success;
            this.status = status;
            this.message = message;
            this.adminId = adminId;
            this.username = username;
        }

        public static LoginResult success(int adminId, String username) {
            return new LoginResult(true, Status.SUCCESS, "Login successful.", adminId, username);
        }

        public static LoginResult invalid(String message) {
            return new LoginResult(false, Status.INVALID, message, 0, null);
        }

        public static LoginResult locked(String message) {
            return new LoginResult(false, Status.LOCKED, message, 0, null);
        }

        public static LoginResult error(String message) {
            return new LoginResult(false, Status.ERROR, message, 0, null);
        }
    }

    public static class ActionResult {
        public final boolean success;
        public final String message;

        private ActionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static ActionResult success(String message) {
            return new ActionResult(true, message);
        }

        public static ActionResult error(String message) {
            return new ActionResult(false, message);
        }
    }

    public static class StaffAccount {
        private final Integer staffId;
        private final String username;
        private final String fullName;
        private final String status;
        private final String createdAt;

        public StaffAccount(int staffId, String username, String fullName, boolean active, String createdAt) {
            this.staffId = staffId;
            this.username = username;
            this.fullName = fullName;
            this.status = active ? "ACTIVE" : "INACTIVE";
            this.createdAt = createdAt;
        }

        public Integer getStaffId() {
            return staffId;
        }

        public String getUsername() {
            return username;
        }

        public String getFullName() {
            return fullName;
        }

        public String getStatus() {
            return status;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }

    public enum Status {
        SUCCESS,
        INVALID,
        LOCKED,
        ERROR
    }
}
