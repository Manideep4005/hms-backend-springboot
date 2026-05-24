package com.hms.dto;

import java.util.List;

public class PatientFullDetailsDto {

    private Long patientId;
    private String name;
    private String email;
    private String mobile;

    private List<AppointmentResponse> appointments;
    private List<BillResponseDto> bills;

    public PatientFullDetailsDto() {
    }

    public PatientFullDetailsDto(Long patientId, String name, String email, String mobile,
            List<AppointmentResponse> appointments,
            List<BillResponseDto> bills) {
        this.patientId = patientId;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.appointments = appointments;
        this.bills = bills;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public List<AppointmentResponse> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<AppointmentResponse> appointments) {
        this.appointments = appointments;
    }

    public List<BillResponseDto> getBills() {
        return bills;
    }

    public void setBills(List<BillResponseDto> bills) {
        this.bills = bills;
    }

}
