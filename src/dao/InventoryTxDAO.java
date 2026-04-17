package dao;

import database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

// Data access for inventory_tx_table.
public class InventoryTxDAO {

    public boolean addTx(int unitId,
                         String txType,
                         Timestamp txAt,
                         int performedBy,
                         String referenceNo,
                         String remarks) {
        String sql = "INSERT INTO inventory_tx_table (unit_id, tx_type, tx_at, performed_by, reference_no, remarks) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, unitId);
            ps.setString(2, txType);
            ps.setTimestamp(3, txAt);
            ps.setInt(4, performedBy);
            ps.setString(5, referenceNo);
            ps.setString(6, remarks);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

