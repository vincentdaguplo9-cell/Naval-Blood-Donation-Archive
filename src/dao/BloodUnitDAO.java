package dao;

import database.DBConnection;
import model.BloodUnit;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// Data access for blood_unit_table.
public class BloodUnitDAO {

    public int addAndReturnId(BloodUnit unit) {
        String sql = "INSERT INTO blood_unit_table (donor_id, blood_type, volume_ml, collection_date, expiry_date, status, session_id, unit_code, component, test_status, storage_location) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, unit.getDonorId());
            ps.setString(2, unit.getBloodType());
            ps.setInt(3, unit.getVolumeMl());
            ps.setDate(4, unit.getCollectionDate());
            ps.setDate(5, unit.getExpiryDate());
            ps.setString(6, unit.getStatus());
            if (unit.getSessionId() == null) {
                ps.setObject(7, null);
            } else {
                ps.setInt(7, unit.getSessionId());
            }
            ps.setString(8, unit.getUnitCode());
            ps.setString(9, unit.getComponent());
            ps.setString(10, unit.getTestStatus());
            ps.setString(11, unit.getStorageLocation());
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

    public boolean updateStatus(int unitId, String status) {
        String sql = "UPDATE blood_unit_table SET status = ? WHERE unit_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, unitId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTestAndStatus(int unitId, String testStatus, String status) {
        String sql = "UPDATE blood_unit_table SET test_status = ?, status = ? WHERE unit_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, testStatus);
            ps.setString(2, status);
            ps.setInt(3, unitId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateLocation(int unitId, String storageLocation) {
        String sql = "UPDATE blood_unit_table SET storage_location = ? WHERE unit_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, storageLocation);
            ps.setInt(2, unitId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<BloodUnit> getUnitsByTestStatus(String testStatus) {
        List<BloodUnit> units = new ArrayList<>();
        String sql = "SELECT * FROM blood_unit_table WHERE test_status = ? ORDER BY unit_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, testStatus);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    units.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return units;
    }

    public List<BloodUnit> getAllUnits() {
        List<BloodUnit> units = new ArrayList<>();
        String sql = "SELECT * FROM blood_unit_table ORDER BY unit_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                units.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return units;
    }

    public List<BloodUnit> getUnitsByStatus(String status) {
        List<BloodUnit> units = new ArrayList<>();
        String sql = "SELECT * FROM blood_unit_table WHERE UPPER(status) = UPPER(?) ORDER BY unit_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    units.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return units;
    }

    public List<BloodUnit> getUnitsFiltered(String status, String testStatus) {
        return getUnitsFiltered(status, testStatus, "All");
    }

    public List<BloodUnit> getUnitsFiltered(String status, String testStatus, String bloodType) {
        List<BloodUnit> units = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM blood_unit_table");
        boolean hasWhere = false;
        if (status != null && !status.equalsIgnoreCase("All")) {
            sql.append(" WHERE UPPER(status) = UPPER(?)");
            hasWhere = true;
        }
        if (testStatus != null && !testStatus.equalsIgnoreCase("All")) {
            sql.append(hasWhere ? " AND " : " WHERE ");
            sql.append("UPPER(test_status) = UPPER(?)");
            hasWhere = true;
        }
        if (bloodType != null && !bloodType.equalsIgnoreCase("All")) {
            sql.append(hasWhere ? " AND " : " WHERE ");
            sql.append("UPPER(blood_type) = UPPER(?)");
        }
        sql.append(" ORDER BY unit_id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (status != null && !status.equalsIgnoreCase("All")) {
                ps.setString(idx++, status);
            }
            if (testStatus != null && !testStatus.equalsIgnoreCase("All")) {
                ps.setString(idx++, testStatus);
            }
            if (bloodType != null && !bloodType.equalsIgnoreCase("All")) {
                ps.setString(idx, bloodType);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    units.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return units;
    }

    public int getTotalUnitsCount() {
        String sql = "SELECT COUNT(*) FROM blood_unit_table";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getAvailableUnitsCount() {
        String sql = "SELECT COUNT(*) FROM blood_unit_table WHERE UPPER(status) = 'AVAILABLE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private BloodUnit mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("unit_id");
        int donorId = rs.getInt("donor_id");
        String bloodType = rs.getString("blood_type");
        int volume = rs.getInt("volume_ml");
        Date collection = rs.getDate("collection_date");
        Date expiry = rs.getDate("expiry_date");
        String status = rs.getString("status");
        Integer sessionId = (Integer) rs.getObject("session_id");
        String unitCode = rs.getString("unit_code");
        String component = rs.getString("component");
        String testStatus = rs.getString("test_status");
        String storageLocation = rs.getString("storage_location");
        return new BloodUnit(id, donorId, bloodType, volume, collection, expiry, status,
                sessionId, unitCode, component, testStatus, storageLocation);
    }
}
