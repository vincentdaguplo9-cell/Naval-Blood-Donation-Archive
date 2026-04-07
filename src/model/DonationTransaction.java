package model;

import java.sql.Timestamp;

// Donation transaction entity model.
public class DonationTransaction {
    private int transactionId;
    private int donorId;
    private int unitId;
    private int staffId;
    private Timestamp transactionDate;
    private String remarks;

    public DonationTransaction() {
    }

    public DonationTransaction(int transactionId, int donorId, int unitId, int staffId,
                               Timestamp transactionDate, String remarks) {
        this.transactionId = transactionId;
        this.donorId = donorId;
        this.unitId = unitId;
        this.staffId = staffId;
        this.transactionDate = transactionDate;
        this.remarks = remarks;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getDonorId() {
        return donorId;
    }

    public void setDonorId(int donorId) {
        this.donorId = donorId;
    }

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public Timestamp getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Timestamp transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
