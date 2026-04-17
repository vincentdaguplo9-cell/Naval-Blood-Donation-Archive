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

    // V2 fields (optional for legacy data).
    private Integer sessionId;
    private String unitCode;
    private String component;
    private String testStatus;
    private String storageLocation;

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

    public BloodUnit(int unitId, int donorId, String bloodType, int volumeMl,
                     Date collectionDate, Date expiryDate, String status,
                     Integer sessionId, String unitCode, String component, String testStatus, String storageLocation) {
        this(unitId, donorId, bloodType, volumeMl, collectionDate, expiryDate, status);
        this.sessionId = sessionId;
        this.unitCode = unitCode;
        this.component = component;
        this.testStatus = testStatus;
        this.storageLocation = storageLocation;
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

    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getTestStatus() {
        return testStatus;
    }

    public void setTestStatus(String testStatus) {
        this.testStatus = testStatus;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }
}
