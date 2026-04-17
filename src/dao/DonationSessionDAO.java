package dao;

import database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

// Data access for donation_session_table.
public class DonationSessionDAO {

    public int createSession(int donorId, Timestamp collectedAt, int collectedBy, String site, String notes) {
        String sql = "INSERT INTO donation_session_table (donor_id, collected_at, collected_by, site, notes) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, donorId);
            ps.setTimestamp(2, collectedAt);
            ps.setInt(3, collectedBy);
            ps.setString(4, site);
            ps.setString(5, notes);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}

