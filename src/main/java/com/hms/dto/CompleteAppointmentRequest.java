package com.hms.dto;

public class CompleteAppointmentRequest {
    private String remarks;
    private String prescription;
    private Boolean needsReview;
    private String reviewTimeperiod;

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    public Boolean getNeedsReview() {
        return needsReview;
    }

    public void setNeedsReview(Boolean needsReview) {
        this.needsReview = needsReview;
    }

    public String getReviewTimeperiod() {
        return reviewTimeperiod;
    }

    public void setReviewTimeperiod(String reviewTimeperiod) {
        this.reviewTimeperiod = reviewTimeperiod;
    }
}
