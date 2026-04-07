package dao;

import database.DBConnection;
import model.DonationTransaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

// Data access for donation_transaction_table.
public class TransactionDAO {

    public boolean addTransaction(DonationTransaction tx) {
        String sql = "INSERT INTO donation_transaction_table (donor_id, unit_id, staff_id, transaction_date, remarks) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tx.getDonorId());
            ps.setInt(2, tx.getUnitId());
            ps.setInt(3, tx.getStaffId());
            ps.setTimestamp(4, tx.getTransactionDate());
            ps.setString(5, tx.getRemarks());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<DonationTransaction> getRecentTransactions(int limit) {
        List<DonationTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM donation_transaction_table ORDER BY transaction_date DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<DonationTransaction> getAllTransactions() {
        List<DonationTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM donation_transaction_table ORDER BY transaction_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private DonationTransaction mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("transaction_id");
        int donorId = rs.getInt("donor_id");
        int unitId = rs.getInt("unit_id");
        int staffId = rs.getInt("staff_id");
        Timestamp ts = rs.getTimestamp("transaction_date");
        String remarks = rs.getString("remarks");
        return new DonationTransaction(id, donorId, unitId, staffId, ts, remarks);
    }
}
