package com.hms.repository;

import java.time.LocalTime;

public interface DoctorAvailabilityProjection {

    Long getId();

    Long getDoctorId();

    String getDayOfWeek();

    LocalTime getStartTime();

    LocalTime getEndTime();

    LocalTime getBreakStart();

    LocalTime getBreakEnd();

    Integer getSlotDuration();
}