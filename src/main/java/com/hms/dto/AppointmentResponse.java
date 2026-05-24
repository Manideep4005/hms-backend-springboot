package com.hms.dto;

import java.time.LocalDateTime;

public class AppointmentResponse {

    private Long id;
    private LocalDateTime appointmentDate;
    private String status;

    private String doctorName;
    private String doctorSpecialization;

    private String patientName;
    private String mobile;
    private Long doctorId;

    public AppointmentResponse() {
    }

    public AppointmentResponse(Long id, LocalDateTime appointmentDate, String status,
            String doctorName, String doctorSpecialization,
            String patientName, String mobile, Long doctorId) {
        this.id = id;
        this.appointmentDate = appointmentDate;
        this.status = status;
        this.doctorName = doctorName;
        this.doctorSpecialization = doctorSpecialization;
        this.patientName = patientName;
        this.mobile = mobile;
        this.doctorId = doctorId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDateTime appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorSpecialization() {
        return doctorSpecialization;
    }

    public void setDoctorSpecialization(String doctorSpecialization) {
        this.doctorSpecialization = doctorSpecialization;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

}