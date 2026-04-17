package dao;

import database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

// Data access for lab_test_table.
public class LabTestDAO {

    public boolean addTest(int unitId,
                           Timestamp testedAt,
                           int testedBy,
                           String hiv,
                           String hbv,
                           String hcv,
                           String syphilis,
                           String malaria,
                           String overall,
                           String remarks) {
        String sql = "INSERT INTO lab_test_table (unit_id, tested_at, tested_by, hiv, hbv, hcv, syphilis, malaria, overall, remarks) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, unitId);
            ps.setTimestamp(2, testedAt);
            ps.setInt(3, testedBy);
            ps.setString(4, hiv);
            ps.setString(5, hbv);
            ps.setString(6, hcv);
            ps.setString(7, syphilis);
            ps.setString(8, malaria);
            ps.setString(9, overall);
            ps.setString(10, remarks);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

