package dao;

import database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// Data access for screening_table.
public class ScreeningDAO {

    public boolean addScreening(int sessionId,
                                Double weightKg,
                                Integer bpSystolic,
                                Integer bpDiastolic,
                                Double hemoglobinGdl,
                                Double temperatureC,
                                boolean passed,
                                String failureReason) {
        String sql = "INSERT INTO screening_table (session_id, weight_kg, bp_systolic, bp_diastolic, hemoglobin_g_dl, temperature_c, passed, failure_reason) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setObject(2, weightKg);
            ps.setObject(3, bpSystolic);
            ps.setObject(4, bpDiastolic);
            ps.setObject(5, hemoglobinGdl);
            ps.setObject(6, temperatureC);
            ps.setBoolean(7, passed);
            ps.setString(8, failureReason);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

