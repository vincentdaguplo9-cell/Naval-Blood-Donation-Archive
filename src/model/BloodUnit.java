package model;

import java.sql.Date;

// Blood unit entity model.
public class BloodUnit {
    private int unitId;
    private int donorId;
    private String bloodType;
    private int volumeMl;
    private Date collectionDate;
    private Date expiryDate;
    private String status;

    public BloodUnit() {
    }

    public BloodUnit(int unitId, int donorId, String bloodType, int volumeMl,
                     Date collectionDate, Date expiryDate, String status) {
        this.unitId = unitId;
        this.donorId = donorId;
        this.bloodType = bloodType;
        this.volumeMl = volumeMl;
        this.collectionDate = collectionDate;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    public int getDonorId() {
        return donorId;
    }

    public void setDonorId(int donorId) {
        this.donorId = donorId;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public int getVolumeMl() {
        return volumeMl;
    }

    public void setVolumeMl(int volumeMl) {
        this.volumeMl = volumeMl;
    }

    public Date getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(Date collectionDate) {
        this.collectionDate = collectionDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
