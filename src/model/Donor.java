package model;

import java.sql.Date;

// Donor entity model.
public class Donor {
    private int donorId;
    private String firstName;
    private String lastName;
    private String bloodType;
    private String contactNo;
    private String address;
    private Date lastDonationDate;
    private String eligibilityStatus;

    public Donor() {
    }

    public Donor(int donorId, String firstName, String lastName, String bloodType, String contactNo,
                 String address, Date lastDonationDate, String eligibilityStatus) {
        this.donorId = donorId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.bloodType = bloodType;
        this.contactNo = contactNo;
        this.address = address;
        this.lastDonationDate = lastDonationDate;
        this.eligibilityStatus = eligibilityStatus;
    }

    public int getDonorId() {
        return donorId;
    }

    public void setDonorId(int donorId) {
        this.donorId = donorId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getLastDonationDate() {
        return lastDonationDate;
    }

    public void setLastDonationDate(Date lastDonationDate) {
        this.lastDonationDate = lastDonationDate;
    }

    public String getEligibilityStatus() {
        return eligibilityStatus;
    }

    public void setEligibilityStatus(String eligibilityStatus) {
        this.eligibilityStatus = eligibilityStatus;
    }
}
