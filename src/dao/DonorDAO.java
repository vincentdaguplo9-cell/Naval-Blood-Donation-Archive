package dao;

import database.DBConnection;
import model.Donor;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Data access for donor_table.
public class DonorDAO {

    public boolean addDonor(Donor donor) {
        String sql = "INSERT INTO donor_table (first_name, last_name, blood_type, contact_no, address, last_donation_date, eligibility_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donor.getFirstName());
            ps.setString(2, donor.getLastName());
            ps.setString(3, donor.getBloodType());
            ps.setString(4, donor.getContactNo());
            ps.setString(5, donor.getAddress());
            ps.setDate(6, donor.getLastDonationDate());
            ps.setString(7, donor.getEligibilityStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateDonor(Donor donor) {
        String sql = "UPDATE donor_table SET first_name = ?, last_name = ?, blood_type = ?, contact_no = ?, address = ?, last_donation_date = ?, eligibility_status = ? WHERE donor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donor.getFirstName());
            ps.setString(2, donor.getLastName());
            ps.setString(3, donor.getBloodType());
            ps.setString(4, donor.getContactNo());
            ps.setString(5, donor.getAddress());
            ps.setDate(6, donor.getLastDonationDate());
            ps.setString(7, donor.getEligibilityStatus());
            ps.setInt(8, donor.getDonorId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteDonor(int donorId) {
        String sql = "DELETE FROM donor_table WHERE donor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, donorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Donor getDonorById(int donorId) {
        String sql = "SELECT * FROM donor_table WHERE donor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, donorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Donor> getAllDonors() {
        List<Donor> donors = new ArrayList<>();
        String sql = "SELECT * FROM donor_table ORDER BY donor_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                donors.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return donors;
    }

    public List<Donor> getRecentDonors(int limit) {
        List<Donor> donors = new ArrayList<>();
        String sql = "SELECT * FROM donor_table ORDER BY donor_id DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    donors.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return donors;
    }

    public List<Donor> searchDonors(String keyword) {
        List<Donor> donors = new ArrayList<>();
        String sql = "SELECT * FROM donor_table WHERE first_name LIKE ? OR last_name LIKE ? OR blood_type LIKE ? ORDER BY donor_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    donors.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return donors;
    }

    public boolean updateDonationStatus(int donorId, Date lastDonationDate, String eligibilityStatus) {
        String sql = "UPDATE donor_table SET last_donation_date = ?, eligibility_status = ? WHERE donor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, lastDonationDate);
            ps.setString(2, eligibilityStatus);
            ps.setInt(3, donorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Donor mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("donor_id");
        String first = rs.getString("first_name");
        String last = rs.getString("last_name");
        String blood = rs.getString("blood_type");
        String contact = rs.getString("contact_no");
        String address = rs.getString("address");
        Date lastDonation = rs.getDate("last_donation_date");
        String eligibility = rs.getString("eligibility_status");
        return new Donor(id, first, last, blood, contact, address, lastDonation, eligibility);
    }
}
