package com.hms.repository;

import java.time.LocalDateTime;

public interface BillProjection {

    Long getId();

    Double getAmount();

    Double getDiscount();

    Double getTax();

    Double getTotalAmount();

    String getStatus();

    LocalDateTime getBillDate();

    Long getAppointmentId();

    String getDoctorName();

    String getSpecialization();

    LocalDateTime getAppointmentDate();

    String getPatientName();
}