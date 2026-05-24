package com.hms.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class BillResponseDto {

    private Long id;
    private Double amount;
    private Double discount;
    private Double tax;
    private Double totalAmount;
    private String status;

    @JsonFormat(pattern = "dd MMM yyyy")
    private LocalDateTime billDate;
    private Long appointmentId;
    private String doctorName;
    private String specialization;

    @JsonFormat(pattern = "dd MMM yyyy HH:mm")
    private LocalDateTime appointmentDate;
    private List<BillItemDto> items;

    public BillResponseDto() {
    }

    public BillResponseDto(Long id, Double amount, Double discount, Double tax,
            Double totalAmount, String status,
            LocalDateTime billDate,
            List<BillItemDto> items, Long appointmentId, String doctorName, String specialization,
            LocalDateTime appointmentDate) {
        this.id = id;
        this.amount = amount;
        this.discount = discount;
        this.tax = tax;
        this.totalAmount = totalAmount;
        this.status = status;
        this.billDate = billDate;
        this.items = items;
        this.appointmentId = appointmentId;
        this.doctorName = doctorName;
        this.specialization = specialization;
        this.appointmentDate = appointmentDate;
    }

    // getters & setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Double getTax() {
        return tax;
    }

    public void setTax(Double tax) {
        this.tax = tax;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDateTime billDate) {
        this.billDate = billDate;
    }

    public List<BillItemDto> getItems() {
        return items;
    }

    public void setItems(List<BillItemDto> items) {
        this.items = items;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDateTime appointmentDate) {
        this.appointmentDate = appointmentDate;
    }
}